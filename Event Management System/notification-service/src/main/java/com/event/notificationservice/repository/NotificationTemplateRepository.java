package com.event.notificationservice.repository;

import com.event.notificationservice.models.entities.NotificationTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate,Long> {


    Optional<NotificationTemplate> findByName(String templateName);
}
