package com.pairflow.question;

import com.pairflow.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "question_answers",
        uniqueConstraints = @UniqueConstraint(name = "uk_answer_dq_user", columnNames = {"dailyQuestionId", "userId"}),
        indexes = @Index(name = "idx_answer_dq", columnList = "dailyQuestionId"))
public class QuestionAnswer extends BaseEntity {

    @Column(nullable = false)
    private String dailyQuestionId;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false, length = 2000)
    private String answer;
}
