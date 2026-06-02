package com.pairflow.audit;

import com.pairflow.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** Immutable record of security-relevant events (spec 9.4). */
@Getter
@Setter
@Entity
@Table(name = "audit_logs", indexes = {
        @Index(name = "idx_audit_actor", columnList = "actorId"),
        @Index(name = "idx_audit_couple", columnList = "coupleId")
})
public class AuditLog extends BaseEntity {

    @Column(nullable = false)
    private String actorId;

    private String coupleId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private AuditAction action;

    private String targetType;
    private String targetId;

    /** Best-effort IP from X-Forwarded-For or RemoteAddr. */
    @Column(length = 64)
    private String ipAddress;
}
