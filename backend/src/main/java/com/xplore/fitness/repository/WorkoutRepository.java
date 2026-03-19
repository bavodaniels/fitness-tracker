package com.xplore.fitness.repository;

import com.xplore.fitness.entity.Workout;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkoutRepository extends JpaRepository<Workout, UUID> {

    List<Workout> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
