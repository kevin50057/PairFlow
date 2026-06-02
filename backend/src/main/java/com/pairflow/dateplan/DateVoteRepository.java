package com.pairflow.dateplan;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DateVoteRepository extends JpaRepository<DateVote, String> {

    List<DateVote> findByCandidateId(String candidateId);

    Optional<DateVote> findByCandidateIdAndUserId(String candidateId, String userId);

    void deleteByCandidateId(String candidateId);
}
