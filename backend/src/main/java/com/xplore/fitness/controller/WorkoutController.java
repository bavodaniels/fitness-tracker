package com.xplore.fitness.controller;

import com.xplore.fitness.dto.WorkoutRequest;
import com.xplore.fitness.dto.WorkoutResponse;
import com.xplore.fitness.entity.ExerciseType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import com.xplore.fitness.entity.User;
import com.xplore.fitness.entity.Workout;
import com.xplore.fitness.repository.UserRepository;
import com.xplore.fitness.repository.WorkoutRepository;
import com.xplore.fitness.service.SessionService;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/workouts")
@Tag(name = "Workouts", description = "Workout logging and retrieval")
public class WorkoutController {

    private final WorkoutRepository workoutRepository;
    private final UserRepository userRepository;
    private final SessionService sessionService;

    public WorkoutController(WorkoutRepository workoutRepository, UserRepository userRepository, SessionService sessionService) {
        this.workoutRepository = workoutRepository;
        this.userRepository = userRepository;
        this.sessionService = sessionService;
    }

    @PostMapping
    @Operation(summary = "Create a new workout log entry")
    @ApiResponse(responseCode = "201", description = "Workout created successfully")
    @ApiResponse(responseCode = "400", description = "Validation error")
    @ApiResponse(responseCode = "401", description = "Authentication required")
    public ResponseEntity<?> createWorkout(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody WorkoutRequest request) {

        Optional<UUID> userId = extractUserId(authHeader);
        if (userId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Authentication required", "timestamp", java.time.Instant.now().toString()));
        }

        ExerciseType exerciseType;
        try {
            exerciseType = ExerciseType.valueOf(request.exerciseType());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid exercise type. Must be DEADLIFT, BACK_SQUAT, or BENCH_PRESS", "timestamp", java.time.Instant.now().toString()));
        }

        User user = userRepository.findById(userId.get()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "User not found"));
        }

        Workout workout = new Workout(user, exerciseType, request.sets(), request.reps(), request.weight());
        workout = workoutRepository.save(workout);

        return ResponseEntity.status(HttpStatus.CREATED).body(WorkoutResponse.from(workout));
    }

    @GetMapping
    @Operation(summary = "Get all workouts for the authenticated user")
    @ApiResponse(responseCode = "200", description = "Workouts retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Authentication required")
    public ResponseEntity<?> getWorkouts(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Optional<UUID> userId = extractUserId(authHeader);
        if (userId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Authentication required", "timestamp", java.time.Instant.now().toString()));
        }

        var workouts = workoutRepository.findByUserIdOrderByCreatedAtDesc(userId.get())
                .stream()
                .map(WorkoutResponse::from)
                .toList();

        return ResponseEntity.ok(workouts);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a specific workout by ID")
    @ApiResponse(responseCode = "200", description = "Workout retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Authentication required")
    @ApiResponse(responseCode = "404", description = "Workout not found")
    public ResponseEntity<?> getWorkout(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable UUID id) {

        Optional<UUID> userId = extractUserId(authHeader);
        if (userId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Authentication required", "timestamp", java.time.Instant.now().toString()));
        }

        var workout = workoutRepository.findById(id);
        if (workout.isEmpty() || !workout.get().getUser().getId().equals(userId.get())) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(WorkoutResponse.from(workout.get()));
    }

    private Optional<UUID> extractUserId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Optional.empty();
        }
        return sessionService.getUserIdFromToken(authHeader.substring(7));
    }
}
