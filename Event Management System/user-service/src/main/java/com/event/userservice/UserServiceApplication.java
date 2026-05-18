package com.event.userservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.beans.factory.annotation.Value;

import com.event.userservice.entity.Role;
import com.event.userservice.entity.User;
import com.event.userservice.entity.UserStatus;
import com.event.userservice.repository.UserRepository;

@SpringBootApplication
@EnableFeignClients
@EnableAsync
public class UserServiceApplication {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceApplication.class);

    // Bootstrap admin credentials — override via env variables in production:
    //   ADMIN_EMAIL, ADMIN_PASSWORD
    @Value("${admin.email:admin@event.com}")
    private String adminEmail;

    @Value("${admin.password:admin123}")
    private String adminPassword;

    @Value("${admin.fullName:System Admin}")
    private String adminFullName;

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }

    // Explicitly set to 'public' to avoid proxying errors
    @Bean
    public CommandLineRunner initAdmin(UserRepository repository, PasswordEncoder encoder) {
        return args -> {
            logger.info("Checking for default Admin user...");

            if (repository.findByEmail(adminEmail).isEmpty()) {
                User admin = new User();
                admin.setFullName(adminFullName);
                admin.setEmail(adminEmail);

                admin.setPassword(encoder.encode(adminPassword));
                admin.setRole(Role.ROLE_ADMIN);
                admin.setStatus(UserStatus.ACTIVE);
                admin.setContactNumber("0000000000");

                repository.save(admin);
                logger.info(">>> Admin user created successfully (email loaded from config).");
            } else {
                logger.info("Admin user already exists in the database.");
            }
        };
    }
}