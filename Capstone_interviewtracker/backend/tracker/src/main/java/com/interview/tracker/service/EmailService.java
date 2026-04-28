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

    public void sendVerificationEmail(User user) {
        // Generate verification token
        String token = UUID.randomUUID().toString();
        LocalDate expiry = LocalDate.now().plusDays(7);

        user.setVerificationToken(token);
        user.setTokenExpiry(expiry);
        userRepository.save(user);

        // Send email
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Verify your Interview Tracker Account");
        message.setFrom("noreply@interviewtracker.com");

        String verificationUrl = baseUrl + "/auth/verify?token=" + token;

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
        } catch (Exception e) {
            System.err.println("Failed to send email: " + e.getMessage());
        }
    }

    public void sendPasswordSetEmail(User user) {
        // Generate a one-time password reset token
        String token = UUID.randomUUID().toString();
        LocalDate expiry = LocalDate.now().plusDays(3);

        user.setVerificationToken(token);
        user.setTokenExpiry(expiry);
        userRepository.save(user);

        // Send email
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Set Your Password - Interview Tracker");
        message.setFrom("noreply@interviewtracker.com");

        String setPasswordUrl = baseUrl + "/auth/set-password?token=" + token;

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
        } catch (Exception e) {
            System.err.println("Failed to send email: " + e.getMessage());
        }
    }

    public void sendPasswordResetEmail(User user) {
        // Generate password reset token
        String token = UUID.randomUUID().toString();
        LocalDate expiry = LocalDate.now().plusDays(1);

        user.setVerificationToken(token);
        user.setTokenExpiry(expiry);
        userRepository.save(user);

        // Send email
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Reset Your Password - Interview Tracker");
        message.setFrom("noreply@interviewtracker.com");

        String resetUrl = baseUrl + "/auth/set-password?token=" + token;

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
        } catch (Exception e) {
            System.err.println("Failed to send email: " + e.getMessage());
        }
    }
}