package com.xplore.fitness.service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class SessionService {

    private final Map<String, UUID> sessions = new ConcurrentHashMap<>();

    public String createSession(UUID userId) {
        String token = UUID.randomUUID().toString();
        sessions.put(token, userId);
        return token;
    }

    public Optional<UUID> getUserIdFromToken(String token) {
        if (token == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(sessions.get(token));
    }

    public void invalidateSession(String token) {
        if (token != null) {
            sessions.remove(token);
        }
    }
}
