package com.example.dialogflow.service;

import com.google.cloud.dialogflow.v2.*;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class DialogflowService {

    private SessionsClient sessionsClient;
    private final String projectId;
    private final String credentialsPath;
    private final String languageCode;

    public DialogflowService(
            @Value("${dialogflow.credentials.path}") String credentialsPath,
            @Value("${dialogflow.project.id}") String projectId,
            @Value("${dialogflow.language.code:en-US}") String languageCode
    ) {
        this.credentialsPath = credentialsPath;
        this.projectId = projectId;
        this.languageCode = languageCode;
    }

    @PostConstruct
    public void init() throws IOException {
        validateConfiguration();
        initializeSessionsClient();
    }

    private void validateConfiguration() {
        if (credentialsPath == null || credentialsPath.isEmpty()) {
            throw new IllegalStateException("Dialogflow credentials path must be configured");
        }
        if (projectId == null || projectId.isEmpty()) {
            throw new IllegalStateException("Dialogflow project ID must be configured");
        }
    }

    private void initializeSessionsClient() throws IOException {
        try (InputStream credentialsStream = getCredentialsStream()) {
            GoogleCredentials credentials = GoogleCredentials.fromStream(credentialsStream);

            SessionsSettings settings = SessionsSettings.newBuilder()
                    .setCredentialsProvider(() -> credentials)
                    .build();

            this.sessionsClient = SessionsClient.create(settings);

            log.info("Successfully initialized Dialogflow client for project {}", projectId);
        } catch (IOException e) {
            throw new IOException("Failed to initialize Dialogflow client. Please verify: " +
                    "1) Credentials file exists at path: " + credentialsPath +
                    " 2) File contains valid JSON service account credentials", e);
        }
    }

    private InputStream getCredentialsStream() throws IOException {
        if (credentialsPath.startsWith("classpath:")) {
            String path = credentialsPath.substring("classpath:".length());
            Resource resource = new ClassPathResource(path);
            return resource.getInputStream();
        }
        return new FileInputStream(credentialsPath);
    }

    public String detectIntent(String message, String sessionId) {
        validateInputs(message, sessionId);

        try {
            SessionName session = SessionName.of(projectId, sessionId);
            QueryInput queryInput = buildQueryInput(message);

            DetectIntentResponse response = sessionsClient.detectIntent(session, queryInput);
            QueryResult queryResult = response.getQueryResult();

            log.debug("Dialogflow response - Intent: {}, Confidence: {}, Fulfillment: {}",
                    queryResult.getIntent().getDisplayName(),
                    queryResult.getIntentDetectionConfidence(),
                    queryResult.getFulfillmentText());

            return queryResult.getFulfillmentText();

        } catch (Exception e) {
            log.error("Failed to detect intent for message: {}, session: {}", message, sessionId, e);
            throw new RuntimeException("Failed to process your message. Please try again.", e);
        }
    }

    private QueryInput buildQueryInput(String message) {
        return QueryInput.newBuilder()
                .setText(TextInput.newBuilder()
                        .setText(message)
                        .setLanguageCode(languageCode))
                .build();
    }

    private void validateInputs(String message, String sessionId) {
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("Message cannot be empty");
        }
        if (sessionId == null || sessionId.isEmpty()) {
            throw new IllegalArgumentException("Session ID cannot be empty");
        }
    }

    @PreDestroy
    public void shutdown() {
        if (sessionsClient != null && !sessionsClient.isShutdown()) {
            try {
                sessionsClient.shutdown();
                if (!sessionsClient.awaitTermination(5, TimeUnit.SECONDS)) {
                    sessionsClient.shutdownNow();
                }
                log.info("Dialogflow client shutdown completed");
            } catch (InterruptedException e) {
                sessionsClient.shutdownNow();
                Thread.currentThread().interrupt();
                log.warn("Dialogflow client shutdown interrupted");
            }
        }
    }

    // Additional methods for event input and context management
    public String detectEventIntent(String eventName, String sessionId) {
        validateInputs(eventName, sessionId);

        try {
            SessionName session = SessionName.of(projectId, sessionId);
            QueryInput queryInput = QueryInput.newBuilder()
                    .setEvent(EventInput.newBuilder()
                            .setName(eventName)
                            .setLanguageCode(languageCode))
                    .build();

            DetectIntentResponse response = sessionsClient.detectIntent(session, queryInput);
            return response.getQueryResult().getFulfillmentText();

        } catch (Exception e) {
            log.error("Failed to detect event intent: {}, session: {}", eventName, sessionId, e);
            throw new RuntimeException("Failed to process event. Please try again.", e);
        }
    }
}