package com.pairflow.event;

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
@Table(name = "events", indexes = {
        @Index(name = "idx_event_couple", columnList = "coupleId"),
        @Index(name = "idx_event_start", columnList = "startTime")
})
public class Event extends BaseEntity {

    @Column(nullable = false)
    private String coupleId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private EventType eventType = EventType.DATE;

    @Column(nullable = false)
    private Instant startTime;

    private Instant endTime;

    private String locationName;
    private String locationAddress;

    private Instant reminderTime;

    // Date-event extras (spec 7.5)
    private Double budget;
    private String transport;
    private String dressCode;
    private String reservationInfo;

    private String relatedTodoId;
    private String relatedAlbumId;

    @Column(nullable = false)
    private String createdBy;
}
