package com.pairflow.notification;

import com.pairflow.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** Stores FCM device tokens for push notification delivery (spec 13.2). */
@Getter
@Setter
@Entity
@Table(name = "user_devices", indexes = @Index(name = "idx_device_user", columnList = "userId"))
public class UserDevice extends BaseEntity {

    @Column(nullable = false)
    private String userId;

    /** Firebase Cloud Messaging registration token. */
    @Column(nullable = false, length = 512)
    private String fcmToken;

    /** ios | android | web */
    @Column(length = 16)
    private String platform;
}
