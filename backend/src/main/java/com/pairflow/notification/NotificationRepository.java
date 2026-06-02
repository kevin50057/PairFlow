package com.pairflow.notification;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, String> {

    List<Notification> findByRecipientIdOrderByCreatedAtDesc(String recipientId, Pageable pageable);

    List<Notification> findByRecipientIdAndReadFalse(String recipientId);

    long countByRecipientIdAndReadFalse(String recipientId);
}
