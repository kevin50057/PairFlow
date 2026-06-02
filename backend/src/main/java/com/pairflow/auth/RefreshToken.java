package com.pairflow.auth;

import com.pairflow.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "refresh_tokens", indexes = {
        @Index(name = "idx_rt_user", columnList = "userId"),
        @Index(name = "idx_rt_hash", columnList = "tokenHash", unique = true)
})
public class RefreshToken extends BaseEntity {

    @Column(nullable = false)
    private String userId;

    /** SHA-256 hex of the raw token. Never store the raw value. */
    @Column(nullable = false, length = 64)
    private String tokenHash;

    @Column(nullable = false)
    private Instant expiresAt;

    private boolean revoked = false;

    private Instant revokedAt;
}
