package com.example.dialogflow.controller;

// ... (existing imports)
import com.example.dialogflow.dto.ChatRequest;
import com.example.dialogflow.dto.ChatResponse;
import com.example.dialogflow.service.DeepseekService;
import com.example.dialogflow.service.DialogflowService;
import com.example.dialogflow.service.OpenAiService;
import com.example.dialogflow.service.SessionIdEmployeeIdMappingService; // New service
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;
import java.util.UUID;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api")
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);
    private final DialogflowService dialogflowService;
    private final OpenAiService openAiService;
    private final DeepseekService deepseekService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate; // Injected RestTemplate
    private final SessionIdEmployeeIdMappingService mappingService; // Inject mapping service

    @Value("${yii2.api.base-url}")
    private String yii2ApiBaseUrl;

    public ChatController(
            DialogflowService dialogflowService,
            OpenAiService openAiService,
            DeepseekService deepseekService,
            RestTemplate restTemplate, // RestTemplate is now correctly injected
            SessionIdEmployeeIdMappingService mappingService // New service injected
    ) {
        this.dialogflowService = dialogflowService;
        this.openAiService = openAiService;
        this.deepseekService = deepseekService;
        this.restTemplate = restTemplate;
        this.mappingService = mappingService; // Initialize
    }

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        try {
            String sessionId = request.getSessionId() != null ?
                    request.getSessionId() : UUID.randomUUID().toString();
            String employeeId = request.getEmployeeId();

            // Store the mapping if employeeId is provided
            if (request.getEmployeeId() != null && !request.getEmployeeId().isEmpty()) {
                mappingService.saveMapping(sessionId, request.getEmployeeId());
                logger.info("Saved mapping: sessionId={} to employeeId={}", sessionId, request.getEmployeeId());
            }

            logger.info("Processing chat request - Session: {}, Employee: {}, Message: {}", sessionId, employeeId, request.getMessage());

            String responseText = dialogflowService.detectIntent(request.getMessage(), sessionId);

            ChatResponse response = new ChatResponse(
                    true,
                    responseText,
                    sessionId,
                    employeeId,
                    null
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error processing chat request", e);

            ChatResponse errorResponse = new ChatResponse(
                    false,
                    "Sorry, I encountered an error processing your request",
                    request.getSessionId(),
                    request.getEmployeeId(),
                    e.getMessage()
            );

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse);
        }
    }

    @PostMapping("/webhook")
    public ResponseEntity<JsonNode> webhook(@RequestBody JsonNode dialogflowRequest) {
        logger.info("Received Dialogflow webhook request: {}", dialogflowRequest.toString());

        try {
            String intentName = dialogflowRequest.path("queryResult").path("intent").path("displayName").asText();
            String sessionId = dialogflowRequest.path("session").asText(); // Extract full session path
            // Dialogflow session path format: projects/project-id/agent/sessions/session-id
            String extractedSessionId = sessionId.substring(sessionId.lastIndexOf('/') + 1); // Get just the session ID part

            logger.info("Webhook triggered for Intent: {}, SessionId: {}", intentName, extractedSessionId);

            // Fetch employeeId using the extracted sessionId
            String employeeId = mappingService.getEmployeeId(extractedSessionId);
            if (employeeId == null) {
                logger.warn("No employeeId found for session: {}. Cannot fetch personalized data.", extractedSessionId);
                ObjectNode errorResponse = objectMapper.createObjectNode();
                errorResponse.put("fulfillmentText", "I'm sorry, I can't retrieve personalized information without knowing your employee ID. Please ensure you are logged in to the HR portal.");
                return ResponseEntity.ok(errorResponse);
            }
            logger.info("Found employeeId: {} for sessionId: {}", employeeId, extractedSessionId);

            JsonNode parametersNode = dialogflowRequest.path("queryResult").path("parameters");
            Map<String, String> parameters = objectMapper.convertValue(parametersNode, Map.class);
            logger.info("Parameters: {}", parameters);

            ObjectNode webhookResponse = objectMapper.createObjectNode();
            String fulfillmentText;

            switch (intentName) {
                case "LeavePolicyInquiry":
                    String leaveType = parameters.get("leaveType");
                    if (leaveType != null && !leaveType.isEmpty()) {
                        // Pass employeeId to the fetch method
                        fulfillmentText = fetchLeavePolicyFromYii2(employeeId, leaveType);
                    } else {
                        fulfillmentText = "Please specify which type of leave you'd like to know about (e.g., annual leave, sick leave).";
                    }
                    webhookResponse.put("fulfillmentText", fulfillmentText);
                    break;

                case "Payroll_Query":
                    String payrollTopic = parameters.get("payrollTopic");
                    if (payrollTopic != null && !payrollTopic.isEmpty()) {
                        // Pass employeeId to the fetch method
                        fulfillmentText = fetchPayrollInfoFromYii2(employeeId, payrollTopic);
                    } else {
                        fulfillmentText = "I can help with payroll questions. Are you asking about pay dates, how to access your pay stub, or something else?";
                    }
                    webhookResponse.put("fulfillmentText", fulfillmentText);
                    break;

                case "HR_Contact_Information":
                    String department = parameters.get("department");
                    if (department != null && !department.isEmpty()) {
                        fulfillmentText = fetchHRContactInfoFromYii2(department); // This might not need employeeId
                    } else {
                        fulfillmentText = "Which HR department's contact information are you looking for? (e.g., Benefits, Payroll, Recruitment, or General HR)";
                    }
                    webhookResponse.put("fulfillmentText", fulfillmentText);
                    break;

                case "AskDeepSeek":
                    String userInput = dialogflowRequest.path("queryResult").path("queryText").asText();
                    fulfillmentText = deepseekService.getCompletion(userInput);
                    webhookResponse.put("fulfillmentText", fulfillmentText);
                    break;

                default:
                    fulfillmentText = "Sorry, I'm unable to process that request at the moment. Please try asking in a different way or contact HR directly.";
                    logger.warn("Unhandled intent in webhook: {}", intentName);
                    webhookResponse.put("fulfillmentText", fulfillmentText);
                    break;
            }

            return ResponseEntity.ok(webhookResponse);

        } catch (Exception e) {
            logger.error("Error processing webhook request", e);
            ObjectNode errorResponse = objectMapper.createObjectNode();
            errorResponse.put("fulfillmentText", "An error occurred while fetching information. Please try again later or contact HR directly if the issue persists.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // --- Modified methods to fetch data from Yii2 API with employeeId ---

    private String fetchLeavePolicyFromYii2(String employeeId, String leaveType) {
        String leaveBalancesEndpoint = yii2ApiBaseUrl + "/leavebalances/index";
        URI uri = UriComponentsBuilder.fromUriString(leaveBalancesEndpoint)
                .queryParam("employee", employeeId) // Pass employeeId
                .queryParam("leavetype", leaveType)
                .build()
                .toUri();

        try {
            logger.info("Calling Yii2 API for leave policy: {}", uri);
            ResponseEntity<JsonNode> response = restTemplate.getForEntity(uri, JsonNode.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // Example: Yii2 returns {"status":"success", "data":{"type":"annual", "balance":20}}
                JsonNode dataNode = response.getBody().path("data");
                if (dataNode.isObject()) {
                    String type = dataNode.path("leavetype").asText();
                    String balance = dataNode.path("balance").asText();
                    if (!type.isEmpty() && !balance.isEmpty()) {
                        return "Your " + type + " leave balance is " + balance + " days.";
                    }
                }
                return "Could not find specific policy or balance details for " + leaveType + ". Please check the HR portal.";
            } else {
                logger.warn("Yii2 API call for leave policy failed with status: {}", response.getStatusCode());
                return "Sorry, I couldn't retrieve your leave balance at this time. Please try again later.";
            }
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            logger.error("HTTP error calling Yii2 API for leave policy ({}): {}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            return "There was an issue connecting to the policy system. Please try again or contact HR directly.";
        } catch (ResourceAccessException e) {
            logger.error("Network error accessing Yii2 API for leave policy: {}", e.getMessage(), e);
            return "I'm having trouble reaching the HR system. Please try again in a moment.";
        } catch (Exception e) {
            logger.error("Unexpected error calling Yii2 API for leave policy: {}", e.getMessage(), e);
            return "An unexpected error occurred while getting leave policy. Please try again.";
        }
    }

    private String fetchPayrollInfoFromYii2(String employeeId, String payrollTopic) {
        String endpoint = yii2ApiBaseUrl + "/payroll/apilist"; // Example endpoint
        URI uri = UriComponentsBuilder.fromUriString(endpoint)
                .queryParam("employee_id", employeeId) // Pass employeeId
                .queryParam("topic", payrollTopic)
                .build()
                .toUri();

        try {
            logger.info("Calling Yii2 API for payroll info: {}", uri);
            ResponseEntity<JsonNode> response = restTemplate.getForEntity(uri, JsonNode.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode dataNode = response.getBody().path("data");
                if (dataNode.isObject()) {
                    if ("pay dates".equalsIgnoreCase(payrollTopic)) {
                        String nextPayDate = dataNode.path("next_pay_date").asText();
                        if (!nextPayDate.isEmpty()) {
                            return "Your next pay date is " + nextPayDate + ".";
                        }
                    } else if ("pay stubs".equalsIgnoreCase(payrollTopic)) {
                        String portalLink = dataNode.path("portal_link").asText();
                        if (!portalLink.isEmpty()) {
                            return "You can access your latest pay stub at: " + portalLink;
                        }
                    }
                    // Add more logic for 'deductions' etc.
                }
                return "Could not find specific payroll information for " + payrollTopic + ". Please check the HR portal.";
            } else {
                logger.warn("Yii2 API call for payroll info failed with status: {}", response.getStatusCode());
                return "Sorry, I couldn't retrieve payroll information at this time. Please try again later.";
            }
        } catch (Exception e) {
            logger.error("Error calling Yii2 API for payroll info: {}", e.getMessage(), e);
            return "An error occurred while fetching payroll details. Please try again.";
        }
    }

    private String fetchHRContactInfoFromYii2(String department) {
        // This might be general info, not needing employeeId
        String endpoint = yii2ApiBaseUrl + "/hr-contacts/apilist";
        URI uri = UriComponentsBuilder.fromUriString(endpoint)
                .queryParam("department", department)
                .build()
                .toUri();

        try {
            logger.info("Calling Yii2 API for HR contact info: {}", uri);
            ResponseEntity<JsonNode> response = restTemplate.getForEntity(uri, JsonNode.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode dataNode = response.getBody().path("data");
                if (dataNode.isObject()) {
                    String contactName = dataNode.path("name").asText();
                    String contactEmail = dataNode.path("email").asText();
                    if (!contactName.isEmpty() && !contactEmail.isEmpty()) {
                        return "For " + department + ", contact " + contactName + " at " + contactEmail + ".";
                    }
                }
                return "Could not find specific contact details for " + department + " department. Please check the HR portal.";
            } else {
                logger.warn("Yii2 API call for HR contact info failed with status: {}", response.getStatusCode());
                return "Sorry, I couldn't retrieve contact information at this time. Please try again later.";
            }
        } catch (Exception e) {
            logger.error("Error calling Yii2 API for HR contact info: {}", e.getMessage(), e);
            return "An error occurred while fetching contact details. Please try again.";
        }
    }
}