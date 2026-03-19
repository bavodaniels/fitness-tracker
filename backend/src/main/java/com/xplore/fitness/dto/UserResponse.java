package com.xplore.fitness.dto;

import com.xplore.fitness.entity.User;
import java.time.Instant;
import java.util.UUID;

public record UserResponse(UUID id, String username, Instant createdAt) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getCreatedAt());
    }
}
