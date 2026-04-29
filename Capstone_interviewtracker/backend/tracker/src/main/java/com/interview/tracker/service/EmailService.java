package com.interview.tracker.service;

import com.interview.tracker.entity.User;
import com.interview.tracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private UserRepository userRepository;

    @Value("${app.base.url:http://localhost:8080}")
    private String baseUrl;

    @Value("${app.frontend.url:http://localhost:5500/frontend/html}")
    private String frontendUrl;

    private String buildFrontendUrl(String page, String token) {
        String normalizedBase = frontendUrl.endsWith("/") ? frontendUrl.substring(0, frontendUrl.length() - 1) : frontendUrl;
        return normalizedBase + "/" + page + "?token=" + token;
    }

    @Value("${spring.mail.username}")
    private String mailUsername;

    public void sendVerificationEmail(User user) {
        String token = UUID.randomUUID().toString();
        LocalDate expiry = LocalDate.now().plusDays(7);

        user.setVerificationToken(token);
        user.setTokenExpiry(expiry);
        userRepository.save(user);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Verify your Interview Tracker Account");
        message.setFrom(mailUsername);

        String verificationUrl = buildFrontendUrl("verify.html", token);
        message.setText(
            "Hello " + user.getName() + ",\n\n" +
            "Welcome to Interview Tracker!\n\n" +
            "Please verify your email address by clicking the link below:\n\n" +
            verificationUrl + "\n\n" +
            "This link will expire in 7 days.\n\n" +
            "After verification, you can set your password to complete your registration.\n\n" +
            "Best regards,\n" +
            "Interview Tracker Team"
        );

        try {
            mailSender.send(message);
            System.out.println("✅ Verification email sent to: " + user.getEmail());
        } catch (Exception e) {
            System.err.println("Failed to send email: " + e.getMessage());
            throw new RuntimeException("Email sending failed", e);
        }
    }

    public void sendPasswordSetEmail(User user) {
        String token = UUID.randomUUID().toString();
        LocalDate expiry = LocalDate.now().plusDays(3);

        user.setVerificationToken(token);
        user.setTokenExpiry(expiry);
        userRepository.save(user);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Set Your Password - Interview Tracker");
        message.setFrom(mailUsername);

        String setPasswordUrl = buildFrontendUrl("set-password.html", token);
        message.setText(
            "Hello " + user.getName() + ",\n\n" +
            "Your account has been verified! Now you need to set your password.\n\n" +
            "Click the link below to set your password:\n\n" +
            setPasswordUrl + "\n\n" +
            "This link will expire in 3 days.\n\n" +
            "If you didn't request this, please ignore this email.\n\n" +
            "Best regards,\n" +
            "Interview Tracker Team"
        );

        try {
            mailSender.send(message);
            System.out.println("✅ Password set email sent to: " + user.getEmail());
        } catch (Exception e) {
            System.err.println("Failed to send email: " + e.getMessage());
            throw new RuntimeException("Email sending failed", e);
        }
    }

    public void sendPasswordResetEmail(User user) {
        String token = UUID.randomUUID().toString();
        LocalDate expiry = LocalDate.now().plusDays(1);

        user.setVerificationToken(token);
        user.setTokenExpiry(expiry);
        userRepository.save(user);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Reset Your Password - Interview Tracker");
        message.setFrom(mailUsername);

        String resetUrl = buildFrontendUrl("set-password.html", token);
        message.setText(
            "Hello " + user.getName() + ",\n\n" +
            "We received a request to reset your password.\n\n" +
            "Click the link below to create a new password:\n\n" +
            resetUrl + "\n\n" +
            "This link will expire in 24 hours.\n\n" +
            "If you didn't request this, please ignore this email or contact support.\n\n" +
            "Best regards,\n" +
            "Interview Tracker Team"
        );

        try {
            mailSender.send(message);
            System.out.println("✅ Password reset email sent to: " + user.getEmail());
        } catch (Exception e) {
            System.err.println("Failed to send email: " + e.getMessage());
            throw new RuntimeException("Email sending failed", e);
        }
    }
}