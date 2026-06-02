package com.pairflow.dateplan;

import com.pairflow.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "date_candidates", indexes = @Index(name = "idx_candidate_plan", columnList = "planId"))
public class DateCandidate extends BaseEntity {

    @Column(nullable = false)
    private String planId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 1000)
    private String description;

    private String location;

    @Column(nullable = false)
    private String addedBy;
}
