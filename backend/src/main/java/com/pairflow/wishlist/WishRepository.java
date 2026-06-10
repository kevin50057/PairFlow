package com.pairflow.wishlist;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface WishRepository extends JpaRepository<Wish, String> {

    List<Wish> findByCoupleIdOrderByCreatedAtDesc(String coupleId);

    List<Wish> findByCoupleIdAndStatusOrderByCreatedAtDesc(String coupleId, WishStatus status);

    List<Wish> findByCoupleIdAndCategoryOrderByCreatedAtDesc(String coupleId, WishCategory category);

    /** Scheduler: active wishes whose scheduled time has already passed. */
    List<Wish> findByStatusAndScheduledAtLessThanEqual(WishStatus status, Instant time);
}
