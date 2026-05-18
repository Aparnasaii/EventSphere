package com.event.notificationservice.services.impls;

import com.event.notificationservice.services.EmailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class EmailServiceImplTest {

    @Autowired
    private EmailService emailService;

    @Test
    void sendEmailSimpleMessage(){
        emailService.sendSimpleMessage("jsontineni@gmail.com","Event Management - Action Required.","Hello, this is a formal test of the EventSphere notification system");
    }

    @Test
    void sendEmailWithAttachment() {
        emailService.sendEmailWithAttachment("jsontineni@gmail.com","TESTING!!!","TESTING SEND MAIL WITH ATTACHMENT","ticket.pdf");
    }

    @Test
    void sendHtmlMessage() {
        emailService.sendHtmlMessage("jsontineni@gmail.com","TESTING!!","JASWANTH SIVA SAI");
    }

    @Test
    void sendBulkEmails() {
        emailService.sendBulkEmails(
                List.of("jsontineni@gmail.com","colab178178@gmail.com"),
                "TESTING!!!!",
                "BULK EMAIL SENDING METHOD TESTING"
        );
    }
}