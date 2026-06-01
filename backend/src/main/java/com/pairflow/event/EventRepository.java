package com.pairflow.event;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, String> {

    List<Event> findByCoupleIdOrderByStartTimeAsc(String coupleId);

    List<Event> findByCoupleIdAndStartTimeBetweenOrderByStartTimeAsc(String coupleId, Instant from, Instant to);
}
