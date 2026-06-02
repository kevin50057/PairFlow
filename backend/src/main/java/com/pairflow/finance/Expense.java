package com.pairflow.finance;

import com.pairflow.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "expenses", indexes = @Index(name = "idx_expense_couple", columnList = "coupleId"))
public class Expense extends BaseEntity {

    @Column(nullable = false)
    private String coupleId;

    @Column(nullable = false)
    private double amount;

    @Column(nullable = false, length = 32)
    private String category = "OTHER";

    @Column(nullable = false)
    private String paidByUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private SplitType splitType = SplitType.NONE;

    /** Payer's share 0..1 when splitType = CUSTOM. */
    private Double customPayerRatio;

    @Column(length = 500)
    private String note;

    private String relatedEventId;

    @Column(nullable = false)
    private Instant spentAt;

    @Column(nullable = false)
    private String createdBy;
}
