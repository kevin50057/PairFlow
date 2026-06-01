package com.pairflow.couple;

import com.pairflow.common.entity.BaseEntity;
import com.pairflow.common.error.ApiException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "couples")
public class Couple extends BaseEntity {

    /** The inviter (A) and the joiner (B). Order is informational only. */
    @Column(nullable = false)
    private String userAId;

    @Column(nullable = false)
    private String userBId;

    private LocalDate relationshipStartDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private CoupleStatus status = CoupleStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    private DataHandling dataHandling;

    private Instant endedAt;

    public boolean hasMember(String userId) {
        return userId.equals(userAId) || userId.equals(userBId);
    }

    /** The other member relative to {@code userId}; guards membership. */
    public String partnerOf(String userId) {
        if (userId.equals(userAId)) return userBId;
        if (userId.equals(userBId)) return userAId;
        throw ApiException.forbidden("Not a member of this couple");
    }
}
