package com.example.notificationaudit.service;

import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.mail.from}")
    private String fromAddress;

    @Autowired
    public EmailService(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    public boolean sendWelcomeEmail(String toEmail, String username, String registeredAt) {
        try {
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("email", toEmail);
            context.setVariable("registeredAt",
                    registeredAt != null ? registeredAt : "N/A");  // no .format() needed
            String htmlContent = templateEngine.process("welcome-email", context);
            sendHtmlEmail(toEmail, "Welcome to UserMgmt, " + username + "!", htmlContent);
            log.info("Welcome email sent to: {}", toEmail);
            return true;
        } catch (Exception e) {
            log.error("Failed to send welcome email to {}: {}", toEmail, e.getMessage());
            return false;
        }
    }

    public boolean sendLoginAlertEmail(String toEmail, String username, String loggedInAt) {
        try {
            Context context = new Context();
            context.setVariable("username", username != null ? username : toEmail);
            context.setVariable("email", toEmail);
            context.setVariable("loggedInAt",
                    loggedInAt != null ? loggedInAt : "N/A");  // no .format() needed
            String htmlContent = templateEngine.process("login-alert-email", context);
            sendHtmlEmail(toEmail, "New Login Detected on Your Account", htmlContent);
            log.info("Login alert email sent to: {}", toEmail);
            return true;
        } catch (Exception e) {
            log.error("Failed to send login alert email to {}: {}", toEmail, e.getMessage());
            return false;
        }
    }

    private void sendHtmlEmail(String to, String subject,
                                String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(fromAddress);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);
        mailSender.send(message);
    }
}