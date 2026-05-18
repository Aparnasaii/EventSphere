package com.event.notificationservice.services;

import java.util.List;

public interface EmailService {

    void sendSimpleMessage(String to,String subject,String text);

    void sendHtmlMessage(String to,String subject,String htmlBody);

    void sendEmailWithAttachment(String to,String subject,String text,String pathTOAttachment);

    void sendHtmlMessageWithAttachment(String to,String subject,String htmlBody,String pathToAttachment);

    void sendBulkEmails(List<String> recipients,String subject,String text);
}
