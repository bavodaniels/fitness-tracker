package com.xplore.fitness.dto;

import java.time.Instant;
import java.util.UUID;

public record LoginResponse(UUID id, String username, Instant createdAt, String token) {
}
