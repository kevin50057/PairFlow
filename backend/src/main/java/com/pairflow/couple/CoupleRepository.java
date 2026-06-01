package com.pairflow.couple;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CoupleRepository extends JpaRepository<Couple, String> {

    @Query("select c from Couple c where c.status = :status and (c.userAId = :uid or c.userBId = :uid)")
    Optional<Couple> findByStatusAndMember(@Param("status") CoupleStatus status, @Param("uid") String uid);
}
