package com.example.notificationservice.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${backend.origin}")
    String origin;

    @Value("${spring.mail.username}")
    String from;

    public void verifyAccountCreationEmail(String to, String token) {
        String link = origin + "/auth/verify-email?token=" + token;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject("Account Creation Confirmation");
        message.setText("Please click the following link to confirm your account creation: " + link);
        javaMailSender.send(message);
    }
}
