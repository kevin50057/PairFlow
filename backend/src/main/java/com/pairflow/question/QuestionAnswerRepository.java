package com.pairflow.question;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuestionAnswerRepository extends JpaRepository<QuestionAnswer, String> {

    List<QuestionAnswer> findByDailyQuestionId(String dailyQuestionId);

    Optional<QuestionAnswer> findByDailyQuestionIdAndUserId(String dailyQuestionId, String userId);
}
