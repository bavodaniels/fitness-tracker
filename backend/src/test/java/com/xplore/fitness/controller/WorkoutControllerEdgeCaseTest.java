package com.xplore.fitness.controller;

import com.jayway.jsonpath.JsonPath;
import com.xplore.fitness.entity.User;
import com.xplore.fitness.repository.UserRepository;
import com.xplore.fitness.service.SessionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class WorkoutControllerEdgeCaseTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SessionService sessionService;

    @Test
    void createWorkoutReturns400ForNegativeSets() throws Exception {
        User user = userRepository.save(new User("user1", "pass"));
        String token = sessionService.createSession(user.getId());

        mockMvc.perform(post("/api/workouts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"exerciseType": "DEADLIFT", "sets": -1, "reps": 5, "weight": 225}
                            """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createWorkoutReturns401WithInvalidToken() throws Exception {
        mockMvc.perform(post("/api/workouts")
                        .header("Authorization", "Bearer invalid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"exerciseType": "DEADLIFT", "sets": 3, "reps": 5, "weight": 225}
                            """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void userCannotAccessOtherUsersWorkout() throws Exception {
        User user1 = userRepository.save(new User("user1", "pass"));
        User user2 = userRepository.save(new User("user2", "pass"));
        String token1 = sessionService.createSession(user1.getId());
        String token2 = sessionService.createSession(user2.getId());

        // User1 creates a workout
        String response = mockMvc.perform(post("/api/workouts")
                        .header("Authorization", "Bearer " + token1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"exerciseType": "BENCH_PRESS", "sets": 3, "reps": 10, "weight": 135}
                            """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String workoutId = JsonPath.read(response, "$.id");

        // User2 cannot access it
        mockMvc.perform(get("/api/workouts/" + workoutId)
                        .header("Authorization", "Bearer " + token2))
                .andExpect(status().isNotFound());
    }

    @Test
    void getWorkoutsReturnsEmptyListForNewUser() throws Exception {
        User user = userRepository.save(new User("newuser", "pass"));
        String token = sessionService.createSession(user.getId());

        mockMvc.perform(get("/api/workouts")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void logoutInvalidatesToken() throws Exception {
        User user = userRepository.save(new User("logoutuser", "pass"));
        String token = sessionService.createSession(user.getId());

        // Token works before logout
        mockMvc.perform(get("/api/workouts")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // Logout
        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // Token no longer works
        mockMvc.perform(get("/api/workouts")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }
}
