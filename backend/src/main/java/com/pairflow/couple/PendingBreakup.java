package com.pairflow.couple;

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

/** Represents a breakup request waiting for the partner's confirmation (spec 7.1 / 9.3). */
@Getter
@Setter
@Entity
@Table(name = "pending_breakups", indexes = @Index(name = "idx_pb_couple", columnList = "coupleId"))
public class PendingBreakup extends BaseEntity {

    @Column(nullable = false)
    private String coupleId;

    @Column(nullable = false)
    private String initiatorId;

    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    private DataHandling dataHandling = DataHandling.ARCHIVE;

    @Column(nullable = false)
    private Instant expiresAt;

    private boolean cancelled = false;
    private Instant cancelledAt;

    private boolean confirmed = false;
    private Instant confirmedAt;
    private String confirmedById;
}
