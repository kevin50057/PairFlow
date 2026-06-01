package com.pairflow.couple;

import com.pairflow.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "couple_invites")
public class CoupleInvite extends BaseEntity {

    @Column(nullable = false, unique = true, length = 16)
    private String code;

    @Column(nullable = false)
    private String inviterUserId;

    @Column(nullable = false)
    private Instant expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private InviteStatus status = InviteStatus.PENDING;

    private String acceptedByUserId;

    private String coupleId;
}
