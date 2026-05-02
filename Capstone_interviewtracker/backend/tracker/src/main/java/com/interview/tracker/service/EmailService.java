package com.interview.tracker.service;

import com.interview.tracker.entity.User;
import com.interview.tracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final UserRepository userRepository;

    public EmailService(JavaMailSender mailSender, UserRepository userRepository) {
        this.mailSender = mailSender;
        this.userRepository = userRepository;
    }

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

    private static final DateTimeFormatter EMAIL_DT = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

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

        String verificationUrl = buildFrontendUrl("verify-password.html", token);
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
            log.info("Verification email sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send verification email to: {}", user.getEmail(), e);
            throw new RuntimeException("Email sending failed", e);
        }
    }

    /**
     * New combined method: Send single email for both verification and password setup
     */
    public void sendVerificationAndPasswordEmail(User user) {
        String token = UUID.randomUUID().toString();
        LocalDate expiry = LocalDate.now().plusDays(7);

        user.setVerificationToken(token);
        user.setTokenExpiry(expiry);
        userRepository.save(user);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Complete Your Interview Tracker Registration");
        message.setFrom(mailUsername);

        String verifyPasswordUrl = buildFrontendUrl("verify-password.html", token);
        message.setText(
            "Hello " + user.getName() + ",\n\n" +
            "Welcome to Interview Tracker!\n\n" +
            "Please complete your registration by clicking the link below:\n\n" +
            verifyPasswordUrl + "\n\n" +
            "This single link will allow you to:\n" +
            "1. Verify your email address\n" +
            "2. Set your secure password\n\n" +
            "This link will expire in 7 days.\n\n" +
            "Best regards,\n" +
            "Interview Tracker Team"
        );

        try {
            mailSender.send(message);
            log.info("Combined verification & password setup email sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send combined email to: {}", user.getEmail(), e);
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

        String setPasswordUrl = buildFrontendUrl("verify-password.html", token);
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
            log.info("Password set email sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send password set email to: {}", user.getEmail(), e);
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

        String resetUrl = buildFrontendUrl("verify-password.html", token);
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
            log.info("Password reset email sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", user.getEmail(), e);
            throw new RuntimeException("Email sending failed", e);
        }
    }

    public void sendPanelAssignmentEmails(User candidateUser, java.util.List<com.interview.tracker.entity.Panel> panels, com.interview.tracker.entity.Interview interview) {
        if (candidateUser == null || candidateUser.getEmail() == null) {
            throw new IllegalArgumentException("Candidate email not found");
        }
        if (panels == null || panels.isEmpty()) {
            throw new IllegalArgumentException("At least one panel member is required");
        }

        String candidateName = candidateUser.getName() == null ? "Candidate" : candidateUser.getName();
        String candidateEmail = candidateUser.getEmail();
        String jobTitle = (interview != null && interview.getCandidate() != null && interview.getCandidate().getJd() != null && interview.getCandidate().getJd().getTitle() != null)
                ? interview.getCandidate().getJd().getTitle()
                : "N/A";
        String round = interview != null ? (interview.getRound() == null ? "N/A" : interview.getRound()) : "N/A";
        String focus = interview != null ? (interview.getFocusArea() == null ? "General" : interview.getFocusArea()) : "General";
        String time = (interview != null && interview.getInterviewTime() != null) ? interview.getInterviewTime().format(EMAIL_DT) : "To be scheduled";

        String panelEmails = panels.stream()
                .map(com.interview.tracker.entity.Panel::getEmail)
                .filter(e -> e != null && !e.isBlank())
                .distinct()
                .collect(Collectors.joining(", "));

        // Email to panel members
        for (com.interview.tracker.entity.Panel p : panels) {
            if (p.getEmail() == null || p.getEmail().isBlank()) continue;

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(p.getEmail());
            message.setSubject("New Interview Assigned - Interview Tracker");
            message.setFrom(mailUsername);

            message.setText(
                    "Hello " + (p.getName() == null ? "Panel Member" : p.getName()) + ",\n\n" +
                    "You have been assigned to an interview.\n\n" +
                    "Candidate: " + candidateName + " (" + candidateEmail + ")\n" +
                    "Job: " + jobTitle + "\n" +
                    "Round: " + round + "\n" +
                    "Focus area: " + focus + "\n" +
                    "Time: " + time + "\n\n" +
                    "Please login to Interview Tracker to view details.\n\n" +
                    "Best regards,\n" +
                    "Interview Tracker Team"
            );
            mailSender.send(message);
        }

        // Email to candidate
        SimpleMailMessage candidateMsg = new SimpleMailMessage();
        candidateMsg.setTo(candidateEmail);
        candidateMsg.setSubject("Panel Assigned to Your Application - Interview Tracker");
        candidateMsg.setFrom(mailUsername);
        candidateMsg.setText(
                "Hello " + candidateName + ",\n\n" +
                "Your application has been assigned to panel member(s): " + panelEmails + ".\n\n" +
                "Job: " + jobTitle + "\n" +
                "Round: " + round + "\n" +
                "Focus area: " + focus + "\n" +
                "Time: " + time + "\n\n" +
                "You will be notified if the interview time changes.\n\n" +
                "Best regards,\n" +
                "Interview Tracker Team"
        );
        mailSender.send(candidateMsg);
    }

    public void sendCandidateSelectedEmail(User candidateUser, String jobTitle) {
        if (candidateUser == null || candidateUser.getEmail() == null || candidateUser.getEmail().isBlank()) return;
        String name = (candidateUser.getName() == null || candidateUser.getName().isBlank()) ? "Candidate" : candidateUser.getName();
        String title = (jobTitle == null || jobTitle.isBlank()) ? "the role" : jobTitle;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(candidateUser.getEmail());
        message.setSubject("Congratulations! You are selected - Interview Tracker");
        message.setFrom(mailUsername);
        message.setText(
                "Hello " + name + ",\n\n" +
                        "Congratulations! You have been selected for " + title + ".\n\n" +
                        "Our HR team will contact you shortly with next steps.\n\n" +
                        "Best regards,\n" +
                        "Interview Tracker Team"
        );
        mailSender.send(message);
        log.info("Selection email sent to: {}", candidateUser.getEmail());
    }

    public void sendCandidateRejectedEmail(User candidateUser, String jobTitle) {
        if (candidateUser == null || candidateUser.getEmail() == null || candidateUser.getEmail().isBlank()) return;
        String name = (candidateUser.getName() == null || candidateUser.getName().isBlank()) ? "Candidate" : candidateUser.getName();
        String title = (jobTitle == null || jobTitle.isBlank()) ? "the role" : jobTitle;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(candidateUser.getEmail());
        message.setSubject("Update on your application - Interview Tracker");
        message.setFrom(mailUsername);
        message.setText(
                "Hello " + name + ",\n\n" +
                        "Thank you for taking the time to interview for " + title + ".\n\n" +
                        "At this moment we will not be moving forward with your application.\n\n" +
                        "We wish you all the best in your job search.\n\n" +
                        "Best regards,\n" +
                        "Interview Tracker Team"
        );
        mailSender.send(message);
        log.info("Rejection email sent to: {}", candidateUser.getEmail());
    }
}