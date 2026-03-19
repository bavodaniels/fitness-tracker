package com.xplore.fitness.dto;

import com.xplore.fitness.entity.Workout;
import java.time.Instant;
import java.util.UUID;

public record WorkoutResponse(UUID id, String exerciseType, Integer sets, Integer reps, Integer weight, Instant createdAt) {

    public static WorkoutResponse from(Workout workout) {
        return new WorkoutResponse(
                workout.getId(),
                workout.getExerciseType().name(),
                workout.getSets(),
                workout.getReps(),
                workout.getWeight(),
                workout.getCreatedAt()
        );
    }
}
