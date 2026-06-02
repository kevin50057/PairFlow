package com.pairflow.notification;

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
@Table(name = "notifications", indexes = @Index(name = "idx_notif_recipient", columnList = "recipientId"))
public class Notification extends BaseEntity {

    @Column(nullable = false)
    private String coupleId;

    @Column(nullable = false)
    private String recipientId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private NotificationType type;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 500)
    private String body;

    @Column(name = "is_read", nullable = false)
    private boolean read = false;

    private String relatedType;
    private String relatedId;

    private Instant readAt;
}
