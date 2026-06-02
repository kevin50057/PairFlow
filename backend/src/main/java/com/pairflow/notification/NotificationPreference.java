package com.pairflow.notification;

import com.pairflow.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "notification_preferences",
        uniqueConstraints = @UniqueConstraint(name = "uk_notifpref_user", columnNames = "userId"))
public class NotificationPreference extends BaseEntity {

    @Column(nullable = false)
    private String userId;

    /** CSV of disabled NotificationType names; everything not listed is enabled. */
    @Column(length = 500)
    private String disabledTypesCsv;
}
