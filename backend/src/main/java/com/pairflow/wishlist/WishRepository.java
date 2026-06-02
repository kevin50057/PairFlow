package com.pairflow.wishlist;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WishRepository extends JpaRepository<Wish, String> {

    List<Wish> findByCoupleIdOrderByCreatedAtDesc(String coupleId);

    List<Wish> findByCoupleIdAndStatusOrderByCreatedAtDesc(String coupleId, WishStatus status);

    List<Wish> findByCoupleIdAndCategoryOrderByCreatedAtDesc(String coupleId, WishCategory category);
}
