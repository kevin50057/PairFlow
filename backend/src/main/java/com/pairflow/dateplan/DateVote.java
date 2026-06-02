package com.pairflow.dateplan;

import com.pairflow.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "date_votes",
        uniqueConstraints = @UniqueConstraint(name = "uk_vote_candidate_user", columnNames = {"candidateId", "userId"}),
        indexes = @Index(name = "idx_vote_candidate", columnList = "candidateId"))
public class DateVote extends BaseEntity {

    @Column(nullable = false)
    private String candidateId;

    @Column(nullable = false)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 8)
    private VoteType vote;
}
