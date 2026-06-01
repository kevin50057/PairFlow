package com.pairflow.mood;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MoodReactionRepository extends JpaRepository<MoodReaction, String> {

    List<MoodReaction> findByMoodEntryIdOrderByCreatedAtAsc(String moodEntryId);
}
