package com.interview.tracker.service;

import com.interview.tracker.dto.LoginRequest;
import com.interview.tracker.dto.RegisterRequest;
import com.interview.tracker.dto.AuthResponse;
import com.interview.tracker.dto.SetPasswordRequest;
import com.interview.tracker.entity.Panel;
import com.interview.tracker.entity.User;
import com.interview.tracker.repository.PanelRepository;
import com.interview.tracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PanelRepository panelRepository;

    @Autowired
    private JwtService jwtService;

    private final List<String> roles = List.of("HR", "PANEL", "CANDIDATE");
    private static final Pattern PASSWORD_POLICY =
            Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{8,}$");

    public AuthResponse register(RegisterRequest request) {


        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }

        if (request.getRole() == null || !roles.contains(request.getRole())) {
            throw new IllegalArgumentException("Invalid role");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        
        user.setPassword(null);
        
        user.setRole(request.getRole());
        
        user.setDateOfBirth(request.getDateOfBirth());
        user.setGender(request.getGender());
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());
        user.setCity(request.getCity());
        user.setState(request.getState());
        user.setCountry(request.getCountry());
        
        user.setEmailVerified(false);
        
        User saved = userRepository.save(user);
        
        emailService.sendVerificationEmail(saved);

        return new AuthResponse(
                "Registration successful! Please check your email to verify your account.",
                saved.getRole(),
                saved.getId(),
                saved.getName(),
                null,
                null
        );
    }

    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!user.isEmailVerified()) {
            throw new IllegalArgumentException("Please verify your email first. Check your inbox for the verification link.");
        }

        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Password not set. Please set your password using the link sent to your email.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid password");
        }

        Long panelId = null;
        if ("PANEL".equals(user.getRole())) {
            Panel panel = panelRepository.findByEmail(user.getEmail()).orElseGet(() -> {
                Panel p = new Panel();
                p.setName(user.getName());
                p.setEmail(user.getEmail());
                p.setExpertise("General");
                return panelRepository.save(p);
            });
            panelId = panel.getId();
        }

        String token = jwtService.generateToken(user);
        return new AuthResponse("Login successful", user.getRole(), user.getId(), user.getName(), panelId, token);
    }

    public AuthResponse verifyEmail(String token) {
        User user = userRepository.findAll().stream()
                .filter(u -> token.equals(u.getVerificationToken()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired verification token"));

        if (user.getTokenExpiry() != null && LocalDate.now().isAfter(user.getTokenExpiry())) {
            throw new IllegalArgumentException("Verification token has expired");
        }

        user.setEmailVerified(true);
        user.setVerificationToken(null);
        user.setTokenExpiry(null);
        userRepository.save(user);

        emailService.sendPasswordSetEmail(user);

        return new AuthResponse(
                "Email verified successfully! Please check your email for the password setup link.",
                user.getRole(),
                user.getId(),
                user.getName(),
                null,
                null
        );
    }

    public AuthResponse setPassword(SetPasswordRequest request) {
        User user = userRepository.findAll().stream()
                .filter(u -> request.getToken().equals(u.getVerificationToken()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired token"));

        if (user.getTokenExpiry() != null && LocalDate.now().isAfter(user.getTokenExpiry())) {
            throw new IllegalArgumentException("Token has expired. Please request a new password reset.");
        }

        validatePasswordPolicy(request.getNewPassword());

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setVerificationToken(null);
        user.setTokenExpiry(null);

        if ("PANEL".equals(user.getRole()) && user.getActivatedAt() == null) {
            user.setActivatedAt(java.time.LocalDateTime.now());
        }
        userRepository.save(user);

        return new AuthResponse(
                "Password set successfully! You can now login.",
                user.getRole(),
                user.getId(),
                user.getName(),
                null,
                null
        );
    }

    public AuthResponse requestPasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Email not found"));

        if (!user.isEmailVerified()) {
            throw new IllegalArgumentException("Please verify your email first");
        }

        emailService.sendPasswordResetEmail(user);

        return new AuthResponse(
                "Password reset link sent to your email!",
                user.getRole(),
                user.getId(),
                user.getName(),
                null,
                null
        );
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    private void validatePasswordPolicy(String password) {
        if (password == null || !PASSWORD_POLICY.matcher(password).matches()) {
            throw new IllegalArgumentException(
                    "Password must be at least 8 characters and include uppercase, lowercase, number, and special character."
            );
        }
    }
}