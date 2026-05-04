package com.interview.tracker.service;

import com.interview.tracker.dto.LoginRequest;
import com.interview.tracker.dto.RegisterRequest;
import com.interview.tracker.dto.AuthResponse;
import com.interview.tracker.dto.SetPasswordRequest;
import com.interview.tracker.dto.CreateTestUserRequest;
import com.interview.tracker.dto.VerifyEmailRequest;
import com.interview.tracker.entity.Panel;
import com.interview.tracker.entity.User;
import com.interview.tracker.repository.PanelRepository;
import com.interview.tracker.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import static com.interview.tracker.constants.AppConstants.ROLE_CANDIDATE;
import static com.interview.tracker.constants.AppConstants.ROLE_HR;
import static com.interview.tracker.constants.AppConstants.ROLE_PANEL;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final PanelRepository panelRepository;
    private final JwtService jwtService;

    private final List<String> roles = List.of(ROLE_HR, ROLE_PANEL, ROLE_CANDIDATE);
    private static final Pattern PASSWORD_POLICY =
            Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{8,}$");

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService,
            PanelRepository panelRepository,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.panelRepository = panelRepository;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {


        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            log.warn("Registration rejected because email already exists: {}", request.getEmail());
            throw new IllegalArgumentException("Email already exists");
        }

        if (userRepository.findByPhone(request.getPhone()).isPresent()) {
            log.warn("Registration rejected because phone already exists for email={}", request.getEmail());
            throw new IllegalArgumentException("Phone already exists");
        }

        if (request.getRole() == null || !roles.contains(request.getRole())) {
            log.warn("Registration rejected because role is invalid: {}", request.getRole());
            throw new IllegalArgumentException("Invalid role");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        
        if (hasText(request.getPassword())) {
            validatePasswordPolicy(request.getPassword());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        } else {
            user.setPassword(null);
        }
        
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
        log.info("Registered user id={} email={} role={}", saved.getId(), saved.getEmail(), saved.getRole());
        
        emailService.sendVerificationAndPasswordEmail(saved);

        return new AuthResponse(
                "Registration successful! Please check your email to verify your account and set your password.",
                saved.getRole(),
                saved.getId(),
                saved.getName(),
                null,
                null
        );
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("Login failed because user was not found: {}", request.getEmail());
                    return new IllegalArgumentException("User not found");
                });

        if (!user.isEmailVerified()) {
            log.warn("Login blocked because email is not verified for user id={}", user.getId());
            throw new IllegalArgumentException("Please verify your email first. Check your inbox for the verification link.");
        }

        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            log.warn("Login blocked because password is not set for user id={}", user.getId());
            throw new IllegalArgumentException("Password not set. Please set your password using the link sent to your email.");
        }

        if (!isPasswordValid(request.getPassword(), user)) {
            log.warn("Login failed because password did not match for user id={}", user.getId());
            throw new IllegalArgumentException("Invalid password");
        }

        Long panelId = null;
        if (ROLE_PANEL.equals(user.getRole())) {
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
        log.info("Login successful for user id={} role={}", user.getId(), user.getRole());
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

    public Map<String, Object> verifyAndPreparePasswordSetup(String token) {
        User user = userRepository.findAll().stream()
                .filter(u -> token.equals(u.getVerificationToken()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired verification token"));

        if (user.getTokenExpiry() != null && LocalDate.now().isAfter(user.getTokenExpiry())) {
            throw new IllegalArgumentException("Verification token has expired");
        }

        user.setEmailVerified(true);
        userRepository.save(user);

        Map<String, Object> response = new java.util.HashMap<>();
        response.put("message", "Email verified successfully. You can now set your password.");
        response.put("email", user.getEmail());
        response.put("userId", user.getId());
        return response;
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
        user.setEmailVerified(true);
        user.setVerificationToken(null);
        user.setTokenExpiry(null);

        if (ROLE_PANEL.equals(user.getRole()) && user.getActivatedAt() == null) {
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

    public AuthResponse resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Email not found"));

        if (user.isEmailVerified() && user.getPassword() != null) {
            throw new IllegalArgumentException("Account is already verified and active");
        }

        emailService.sendVerificationAndPasswordEmail(user);

        return new AuthResponse(
                "Verification email resent successfully! Please check your inbox.",
                user.getRole(),
                user.getId(),
                user.getName(),
                null,
                null
        );
    }

    private void validatePasswordPolicy(String password) {
        if (password == null || !PASSWORD_POLICY.matcher(password).matches()) {
            throw new IllegalArgumentException(
                    "Password must be at least 8 characters and include uppercase, lowercase, number, and special character."
            );
        }
    }

    private boolean isPasswordValid(String rawPassword, User user) {
        String storedPassword = user.getPassword();
        if (passwordEncoder.matches(rawPassword, storedPassword)) {
            return true;
        }

        if (storedPassword != null && storedPassword.equals(rawPassword)) {
            validatePasswordPolicy(rawPassword);
            user.setPassword(passwordEncoder.encode(rawPassword));
            userRepository.save(user);
            log.info("Upgraded plaintext password to BCrypt for user id={}", user.getId());
            return true;
        }

        return false;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    public AuthResponse createTestUser(CreateTestUserRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }

        if (request.getRole() == null || !roles.contains(request.getRole())) {
            throw new IllegalArgumentException("Invalid role. Must be HR, PANEL, or CANDIDATE");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setPhone(request.getPhone());
        user.setCity(request.getCity());
        user.setState(request.getState());
        user.setCountry(request.getCountry());
        user.setEmailVerified(true);

        User saved = userRepository.save(user);

        if (ROLE_PANEL.equals(user.getRole())) {
            Panel panel = new Panel();
            panel.setName(user.getName());
            panel.setEmail(user.getEmail());
            panel.setExpertise("General");
            panelRepository.save(panel);
        }

        return new AuthResponse(
                "Test user created successfully!",
                saved.getRole(),
                saved.getId(),
                saved.getName(),
                null,
                null
        );
    }
}
