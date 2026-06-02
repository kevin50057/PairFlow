package com.pairflow.dateplan;

import com.pairflow.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "date_plans", indexes = @Index(name = "idx_dateplan_couple", columnList = "coupleId"))
public class DatePlan extends BaseEntity {

    @Column(nullable = false)
    private String coupleId;

    @Column(nullable = false, length = 200)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private DateType dateType = DateType.FOOD;

    @Enumerated(EnumType.STRING)
    @Column(length = 8)
    private BudgetLevel budgetLevel;

    private String area;
    private Integer durationHours;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private PlanStatus status = PlanStatus.PLANNING;

    private String chosenCandidateId;
    private String scheduledEventId;

    @Column(nullable = false)
    private String createdBy;
}
