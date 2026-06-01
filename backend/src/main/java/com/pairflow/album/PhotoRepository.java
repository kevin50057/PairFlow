package com.pairflow.album;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PhotoRepository extends JpaRepository<Photo, String> {

    List<Photo> findByCoupleIdOrderByCreatedAtDesc(String coupleId);

    List<Photo> findByCoupleIdAndAlbumIdOrderByCreatedAtDesc(String coupleId, String albumId);

    List<Photo> findByCoupleIdAndFavoriteTrueOrderByCreatedAtDesc(String coupleId);

    List<Photo> findByCoupleId(String coupleId);

    Optional<Photo> findByStorageKey(String storageKey);

    long countByAlbumId(String albumId);
}
