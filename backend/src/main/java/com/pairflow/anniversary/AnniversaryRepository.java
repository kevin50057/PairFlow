package com.pairflow.anniversary;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnniversaryRepository extends JpaRepository<Anniversary, String> {

    List<Anniversary> findByCoupleId(String coupleId);
}
