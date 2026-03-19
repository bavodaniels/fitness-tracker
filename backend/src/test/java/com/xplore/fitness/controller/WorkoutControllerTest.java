package com.xplore.fitness.controller;

import com.jayway.jsonpath.JsonPath;
import com.xplore.fitness.entity.User;
import com.xplore.fitness.repository.UserRepository;
import com.xplore.fitness.service.SessionService;
import org.junit.jupiter.api.BeforeEach;
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
class WorkoutControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SessionService sessionService;

    private String token;

    @BeforeEach
    void setUp() {
        User user = userRepository.save(new User("testuser", "password"));
        token = sessionService.createSession(user.getId());
    }

    @Test
    void createWorkoutReturns201() throws Exception {
        mockMvc.perform(post("/api/workouts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"exerciseType": "DEADLIFT", "sets": 3, "reps": 5, "weight": 225}
                            """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.exerciseType").value("DEADLIFT"))
                .andExpect(jsonPath("$.sets").value(3))
                .andExpect(jsonPath("$.reps").value(5))
                .andExpect(jsonPath("$.weight").value(225));
    }

    @Test
    void createWorkoutReturns401WithoutAuth() throws Exception {
        mockMvc.perform(post("/api/workouts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"exerciseType": "DEADLIFT", "sets": 3, "reps": 5, "weight": 225}
                            """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createWorkoutReturns400ForInvalidExerciseType() throws Exception {
        mockMvc.perform(post("/api/workouts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"exerciseType": "INVALID", "sets": 3, "reps": 5, "weight": 225}
                            """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getWorkoutsReturnsUserWorkouts() throws Exception {
        mockMvc.perform(post("/api/workouts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"exerciseType": "BENCH_PRESS", "sets": 4, "reps": 8, "weight": 135}
                            """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/workouts")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].exerciseType").value("BENCH_PRESS"));
    }

    @Test
    void getWorkoutByIdReturnsWorkout() throws Exception {
        String response = mockMvc.perform(post("/api/workouts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"exerciseType": "BACK_SQUAT", "sets": 5, "reps": 5, "weight": 315}
                            """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String workoutId = JsonPath.read(response, "$.id");

        mockMvc.perform(get("/api/workouts/" + workoutId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exerciseType").value("BACK_SQUAT"));
    }
}
