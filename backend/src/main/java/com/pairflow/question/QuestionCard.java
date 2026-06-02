package com.pairflow.question;

import com.pairflow.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** Catalog of couple questions, seeded at startup (spec 7.9). */
@Getter
@Setter
@Entity
@Table(name = "question_cards")
public class QuestionCard extends BaseEntity {

    @Column(nullable = false, length = 500)
    private String text;

    @Column(nullable = false, length = 32)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 8)
    private Sensitivity sensitivity = Sensitivity.LOW;
}
