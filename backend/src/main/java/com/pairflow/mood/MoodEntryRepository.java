package com.pairflow.mood;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface MoodEntryRepository extends JpaRepository<MoodEntry, String> {

    Optional<MoodEntry> findFirstByCoupleIdAndUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(
            String coupleId, String userId, Instant from, Instant to);

    List<MoodEntry> findByCoupleIdOrderByCreatedAtDesc(String coupleId, Pageable pageable);
}
