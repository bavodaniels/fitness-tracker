package com.xplore.fitness.controller;

import com.xplore.fitness.dto.LoginRequest;
import com.xplore.fitness.dto.LoginResponse;
import com.xplore.fitness.dto.RegisterRequest;
import com.xplore.fitness.dto.UserResponse;
import com.xplore.fitness.entity.User;
import com.xplore.fitness.repository.UserRepository;
import com.xplore.fitness.service.SessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "User registration, login, and logout")
public class AuthController {

    private final UserRepository userRepository;
    private final SessionService sessionService;

    public AuthController(UserRepository userRepository, SessionService sessionService) {
        this.userRepository = userRepository;
        this.sessionService = sessionService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    @ApiResponse(responseCode = "201", description = "User registered successfully")
    @ApiResponse(responseCode = "400", description = "Validation error or username taken")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Username already exists",
                    "timestamp", java.time.Instant.now().toString()
            ));
        }

        User user = new User(request.username(), request.password());
        user = userRepository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(user));
    }

    @PostMapping("/login")
    @Operation(summary = "Log in with username and password")
    @ApiResponse(responseCode = "200", description = "Login successful")
    @ApiResponse(responseCode = "401", description = "Invalid credentials")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        var userOpt = userRepository.findByUsername(request.username());
        if (userOpt.isEmpty() || !userOpt.get().getPassword().equals(request.password())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "error", "Invalid credentials",
                    "timestamp", java.time.Instant.now().toString()
            ));
        }

        User user = userOpt.get();
        String token = sessionService.createSession(user.getId());

        return ResponseEntity.ok(new LoginResponse(user.getId(), user.getUsername(), user.getCreatedAt(), token));
    }

    @PostMapping("/logout")
    @Operation(summary = "Log out the current user")
    @ApiResponse(responseCode = "200", description = "Logged out successfully")
    public ResponseEntity<?> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            sessionService.invalidateSession(authHeader.substring(7));
        }
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }
}
