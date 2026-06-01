package com.pairflow.anniversary;

import com.pairflow.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "anniversaries", indexes = @Index(name = "idx_anniv_couple", columnList = "coupleId"))
public class Anniversary extends BaseEntity {

    @Column(nullable = false)
    private String coupleId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private RepeatType repeatType = RepeatType.YEARLY;

    /** Reminder offsets in days, stored CSV e.g. "30,7,3,1,0" (spec 7.4). */
    @Column(length = 100)
    private String reminderDaysBeforeCsv;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private String createdBy;
}
