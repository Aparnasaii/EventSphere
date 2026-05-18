package com.event.notificationservice.repository;

import com.event.notificationservice.models.entities.Notification;
import com.event.notificationservice.models.enums.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification,Long> {
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Notification> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, NotificationStatus notificationStatus);
}
