package com.example.dialogflow.service;

import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.LoggerFactory;

@Service
public class SessionIdEmployeeIdMappingService {

    private static final Logger logger = LoggerFactory.getLogger(SessionIdEmployeeIdMappingService.class);
    // In-memory map to store sessionId -> employeeId mappings
    // IMPORTANT: For production, replace this with a distributed cache like Redis
    private final ConcurrentHashMap<String, String> sessionIdToEmployeeIdMap = new ConcurrentHashMap<>();

    // Optional: A cleanup scheduler to prevent memory leaks for expired sessions
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final long SESSION_TIMEOUT_MINUTES = 60; // Sessions expire after 60 minutes of inactivity

    public SessionIdEmployeeIdMappingService() {
        // Schedule cleanup task to run periodically
        scheduler.scheduleAtFixedRate(this::cleanupExpiredSessions, 10, 30, TimeUnit.MINUTES);
    }

    public void saveMapping(String sessionId, String employeeId) {
        sessionIdToEmployeeIdMap.put(sessionId, employeeId);
        logger.debug("Mapping saved: {} -> {}", sessionId, employeeId);
    }

    public String getEmployeeId(String sessionId) {
        String employeeId = sessionIdToEmployeeIdMap.get(sessionId);
        logger.debug("Mapping retrieved: {} -> {}", sessionId, employeeId);
        return employeeId;
    }

    // This cleanup method is highly simplified and assumes you're tracking last activity
    // For a real system with ConcurrentHashMap, you'd need to store last-access-time
    // within a custom value object. With Redis, TTL (Time To Live) handles this naturally.
    private void cleanupExpiredSessions() {
        // This is a placeholder. A robust cleanup would involve tracking the last access time
        // for each session and removing entries older than SESSION_TIMEOUT_MINUTES.
        // For ConcurrentHashMap, you'd need to store a custom object like:
        // class SessionInfo { String employeeId; long lastAccessTime; }
        // For Redis, you'd use EXPIRE commands.
        logger.info("Cleaning up expired sessions (placeholder - no actual cleanup for ConcurrentHashMap without last-access tracking)");
        // Example if using a more complex map value with timestamp:
        // sessionIdToEmployeeIdMap.entrySet().removeIf(entry ->
        //     System.currentTimeMillis() - entry.getValue().lastAccessTime > TimeUnit.MINUTES.toMillis(SESSION_TIMEOUT_MINUTES)
        // );
    }

    @PreDestroy
    public void shutdown() {
        scheduler.shutdown();
        logger.info("Session cleanup scheduler shut down.");
    }
}