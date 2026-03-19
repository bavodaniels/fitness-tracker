package com.xplore.fitness.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record WorkoutRequest(
        @NotNull(message = "Exercise type is required") String exerciseType,
        @NotNull(message = "Sets is required") @Positive(message = "Sets must be positive") Integer sets,
        @NotNull(message = "Reps is required") @Positive(message = "Reps must be positive") Integer reps,
        @NotNull(message = "Weight is required") @Positive(message = "Weight must be positive") Integer weight) {
}
