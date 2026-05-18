package com.event.notificationservice.services.impls;

import com.event.notificationservice.services.EmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    private JavaMailSender mailSender;
    @Value("${api.mail.from}")
    private String fromAddress;

    public EmailServiceImpl(JavaMailSender mailSender){
        this.mailSender=mailSender;
    }
    @Async
    @Override
    public void sendSimpleMessage(String toAddress, String subject, String text) {
        log.info("Initiating simple email delivery to: {}", toAddress);
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(toAddress);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
            log.info("Simple email successfully dispatched to: {}", toAddress);
        } catch (Exception ex) {
            log.error("CRITICAL: Failed to send simple email to {}. Error: {}", toAddress, ex.getMessage());
        }
    }
    @Async
    @Override
    public void sendEmailWithAttachment(String toAddress, String subject, String text, String pathToAttachment) {
        log.info("Initiating email delivery with attachment to: {}", toAddress);
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(message, true);
            mimeMessageHelper.setFrom(fromAddress);
            mimeMessageHelper.setTo(toAddress);
            mimeMessageHelper.setSubject(subject);
            mimeMessageHelper.setText(text);

            mimeMessageHelper.addAttachment(pathToAttachment, new ClassPathResource(pathToAttachment));
            log.debug("Attachment successfully added from path: {}", pathToAttachment);

            mailSender.send(message);
            log.info("Email with attachment successfully sent to: {}", toAddress);
        } catch (Exception ex) {
            log.error("CRITICAL: Failed to send email with attachment to {}. Path: {}. Error: {}",
                    toAddress, pathToAttachment, ex.getMessage());
        }
    }
    @Async
    @Override
    public void sendHtmlMessage(String toAddress, String subject, String htmlBody) {
        log.info("Initiating HTML email delivery to: {}", toAddress);
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(message, true);
            mimeMessageHelper.setFrom(fromAddress);
            mimeMessageHelper.setTo(toAddress);
            mimeMessageHelper.setSubject(subject);
            mimeMessageHelper.setText(htmlBody, true);

            mailSender.send(message);
            log.info("HTML email successfully sent to: {}", toAddress);
        } catch (Exception ex) {
            log.error("CRITICAL: Failed to send HTML email to {}. Error: {}", toAddress, ex.getMessage());
        }
    }
    @Async
    @Override
    public void sendHtmlMessageWithAttachment(String toAddress, String subject, String htmlBody, String pathToAttachment) {
        log.info("Initiating HTML email delivery with attachment to: {}", toAddress);
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(message, true);
            mimeMessageHelper.setFrom(fromAddress);
            mimeMessageHelper.setTo(toAddress);
            mimeMessageHelper.setSubject(subject);

            mimeMessageHelper.addAttachment(pathToAttachment, new ClassPathResource(pathToAttachment));
            log.debug("Attachment successfully added for HTML mail: {}", pathToAttachment);

            mimeMessageHelper.setText(htmlBody, true);
            mailSender.send(message);
            log.info("HTML email with attachment successfully sent to: {}", toAddress);
        } catch (Exception ex) {
            log.error("CRITICAL: Failed to send HTML email with attachment to {}. Error: {}", toAddress, ex.getMessage());
        }
    }
    @Async
    @Override
    public void sendBulkEmails(List<String> recipients, String subject, String text) {
        log.info("Initiating bulk email dispatch for {} recipients", recipients.size());
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(recipients.toArray(String[]::new));
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
            log.info("Bulk emails successfully handed off to SMTP server.");
        } catch (Exception ex) {
            log.error("CRITICAL: Bulk email dispatch failed. Error: {}", ex.getMessage());
        }
    }
    public static String generateHtml(String text){
        return "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <title>Hello Page</title>\n" +
                "<style>\n" +
                "        body {\n" +
                "            font-family: Arial, sans-serif;\n" +
                "            background: linear-gradient(to right, #74ebd5, #ACB6E5);\n" +
                "            margin: 0;\n" +
                "            padding: 0;\n" +
                "            display: flex;\n" +
                "            height: 100vh;\n" +
                "            justify-content: center;\n" +
                "            align-items: center;\n" +
                "        }\n" +
                "        h1 {\n" +
                "            color: #333;\n" +
                "            background-color: #fff;\n" +
                "            padding: 20px 40px;\n" +
                "            border-radius: 10px;\n" +
                "            box-shadow: 0 4px 10px rgba(0,0,0,0.2);\n" +
                "            font-size: 2.5rem;\n" +
                "            text-align: center;\n" +
                "        }\n" +
                "    </style>"+
                "</head>\n" +
                "<body>\n" +
                "<h1>"+text+"</h1>\n" +
                "</body>\n" +
                "</html>";
    }
}
