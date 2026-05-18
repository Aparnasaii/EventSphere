package com.event.notificationservice.services.impls;

import com.event.notificationservice.models.dtos.BulkNotificationRequestDTO;
import com.event.notificationservice.models.dtos.NotificationRequestDTO;
import com.event.notificationservice.models.dtos.NotificationResponseDTO;
import com.event.notificationservice.models.dtos.RecipientDetails;
import com.event.notificationservice.models.entities.Notification;
import com.event.notificationservice.models.entities.NotificationTemplate;
import com.event.notificationservice.models.enums.NotificationStatus;
import com.event.notificationservice.models.enums.NotificationType;
import com.event.notificationservice.repository.NotificationRepository;
import com.event.notificationservice.repository.NotificationTemplateRepository;
import com.event.notificationservice.services.EmailService;
import com.event.notificationservice.services.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationTemplateRepository templateRepository;
    private final EmailService emailService;

    public NotificationServiceImpl(NotificationRepository notificationRepository, NotificationTemplateRepository templateRepository, EmailService emailService) {
        this.emailService = emailService;
        this.notificationRepository = notificationRepository;
        this.templateRepository = templateRepository;
    }

    @Override
    @Transactional
    public NotificationResponseDTO sendNotification(NotificationRequestDTO request) {
        log.info("Processing single notification for User ID: {}, Template: {}", request.getUserId(), request.getTemplateName());

        try {
            // 1. Fetch the template
            NotificationTemplate template = templateRepository.findByName(request.getTemplateName())
                    .orElseThrow(() -> {
                        log.error("Notification failed: Template '{}' not found in database", request.getTemplateName());
                        return new RuntimeException("Template not found: " + request.getTemplateName());
                    });

            // 2. Render HTML content
            log.debug("Rendering templates for recipient: {}", request.getRecipient());
            String finalSubject = render(template.getSubjectTemplate(), request.getTemplateData());
            String finalMessage = render(template.getBodyTemplate(), request.getTemplateData());

            // 3. Save record to DB
            LocalDateTime now = LocalDateTime.now();
            Notification notification = Notification.builder()
                    .userId(request.getUserId())
                    .recipient(request.getRecipient())
                    .subject(finalSubject)
                    .message(finalMessage)
                    .type(request.getType())
                    .status(NotificationStatus.SENT)
                    .sentAt(now)
                    .build();

            Notification saved = notificationRepository.save(notification);
            log.info("Notification record saved to DB with ID: {}", saved.getNotificationId());

            // 4. Send via EmailService
            if (NotificationType.EMAIL.equals(request.getType())) {
                log.info("Dispatching email to: {}", request.getRecipient());
                emailService.sendHtmlMessage(request.getRecipient(), finalSubject, finalMessage);
            }

            return mapToResponse(saved);

        } catch (Exception e) {
            log.error("Critical error in sendNotification for User {}: {}", request.getUserId(), e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional
    public void sendBulkNotifications(BulkNotificationRequestDTO bulkRequest) {
        int recipientCount = bulkRequest.getRecipients() != null ? bulkRequest.getRecipients().size() : 0;
        log.info("Processing bulk notification. Template: {}, Recipients: {}", bulkRequest.getTemplateName(), recipientCount);

        NotificationTemplate template = templateRepository.findByName(bulkRequest.getTemplateName())
                .orElseThrow(() -> {
                    log.error("Bulk process aborted: Template '{}' not found", bulkRequest.getTemplateName());
                    return new RuntimeException("Template not found");
                });

        List<Notification> notificationsToSave = new ArrayList<>();

        for (RecipientDetails recipient : bulkRequest.getRecipients()) {
            Map<String, String> data = new HashMap<>(bulkRequest.getCommonData());
            if (recipient.getPersonalData() != null) {
                data.putAll(recipient.getPersonalData());
            }

            String subject = render(template.getSubjectTemplate(), data);
            String message = render(template.getBodyTemplate(), data);

            notificationsToSave.add(Notification.builder()
                    .userId(recipient.getUserId())
                    .recipient(recipient.getEmail())
                    .subject(subject)
                    .message(message)
                    .type(bulkRequest.getType())
                    .status(NotificationStatus.SENT)
                    .sentAt(LocalDateTime.now())
                    .build());
        }

        notificationRepository.saveAll(notificationsToSave);
        log.info("Successfully saved {} notifications to database", notificationsToSave.size());

        if (NotificationType.EMAIL.equals(bulkRequest.getType())) {
            log.info("Dispatching bulk emails...");
            notificationsToSave.forEach(n ->
                    emailService.sendHtmlMessage(n.getRecipient(), n.getSubject(), n.getMessage())
            );
            log.info("Bulk email dispatch completed.");
        }
    }

    @Override
    public List<NotificationResponseDTO> getUserNotifications(Long userId) {
        log.info("Fetching all notifications for User ID: {}", userId);
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<NotificationResponseDTO> getUnreadNotifications(Long userId) {
        log.info("Fetching unread notifications for User ID: {}", userId);
        return notificationRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, NotificationStatus.SENT)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId) {
        log.debug("Marking notification {} as READ", notificationId);
        notificationRepository.findById(notificationId).ifPresentOrElse(n -> {
            n.setStatus(NotificationStatus.READ);
            notificationRepository.save(n);
            log.info("Notification {} status updated to READ", notificationId);
        }, () -> log.warn("Attempted to mark as read, but notification {} was not found", notificationId));
    }

    @Override
    @Transactional
    public void deleteNotification(Long notificationId) {
        log.info("Deleting notification record ID: {}", notificationId);
        notificationRepository.deleteById(notificationId);
    }

    private String render(String text, Map<String, String> data) {
        if (text == null) return "";
        if (data == null) return text;
        String renderedText = text;
        for (Map.Entry<String, String> entry : data.entrySet()) {
            renderedText = renderedText.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        return renderedText;
    }

    private NotificationResponseDTO mapToResponse(Notification n) {
        return NotificationResponseDTO.builder()
                .id(n.getNotificationId())
                .userId(n.getUserId())
                .subject(n.getSubject())
                .message(n.getMessage())
                .type(n.getType().toString())
                .status(n.getStatus().toString())
                .sentAt(n.getSentAt() != null ? n.getSentAt() : n.getCreatedAt())
                .build();
    }
}