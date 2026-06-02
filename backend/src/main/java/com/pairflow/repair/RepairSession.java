package com.pairflow.repair;

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
@Table(name = "repair_sessions", indexes = @Index(name = "idx_repair_couple", columnList = "coupleId"))
public class RepairSession extends BaseEntity {

    @Column(nullable = false)
    private String coupleId;

    @Column(nullable = false)
    private String initiatorId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private RepairState state;

    /** Raw feelings — visible only to the initiator; the partner sees the softened version. */
    @Column(length = 2000)
    private String feelings;

    @Column(length = 1000)
    private String keyPoints;

    @Column(length = 2000)
    private String softenedMessage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private RepairStatus status = RepairStatus.DRAFT;

    @Column(nullable = false)
    private boolean flagged = false;

    private String responderId;

    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    private ResponseType responseType;

    @Column(length = 1000)
    private String responseNote;

    private Instant respondedAt;
}
