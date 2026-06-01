package com.pairflow.album;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlbumRepository extends JpaRepository<Album, String> {

    List<Album> findByCoupleIdOrderByCreatedAtDesc(String coupleId);
}
