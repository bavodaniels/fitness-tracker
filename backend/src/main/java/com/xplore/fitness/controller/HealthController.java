package com.xplore.fitness.controller;

import java.util.Map;
import javax.sql.DataSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    private final DataSource dataSource;

    public HealthController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        try (var conn = dataSource.getConnection()) {
            conn.isValid(2);
            return ResponseEntity.ok(Map.of(
                "service", "fitness-tracker",
                "status", "UP"
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "service", "fitness-tracker",
                "status", "DOWN"
            ));
        }
    }
}
