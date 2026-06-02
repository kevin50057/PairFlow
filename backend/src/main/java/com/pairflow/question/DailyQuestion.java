package com.pairflow.question;

import com.pairflow.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/** The question assigned to a couple on a given day. */
@Getter
@Setter
@Entity
@Table(name = "daily_questions",
        uniqueConstraints = @UniqueConstraint(name = "uk_daily_couple_date", columnNames = {"coupleId", "date"}),
        indexes = @Index(name = "idx_daily_couple", columnList = "coupleId"))
public class DailyQuestion extends BaseEntity {

    @Column(nullable = false)
    private String coupleId;

    @Column(nullable = false)
    private String questionCardId;

    @Column(nullable = false)
    private LocalDate date;

    @Column(name = "is_favorite", nullable = false)
    private boolean favorite = false;
}
