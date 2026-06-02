package com.pairflow.question;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyQuestionRepository extends JpaRepository<DailyQuestion, String> {

    Optional<DailyQuestion> findByCoupleIdAndDate(String coupleId, LocalDate date);

    List<DailyQuestion> findByCoupleIdOrderByDateDesc(String coupleId);
}
