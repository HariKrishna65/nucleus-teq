package com.interview.tracker.service;

import com.interview.tracker.dto.AuthResponse;
import com.interview.tracker.dto.CreateTestUserRequest;
import com.interview.tracker.dto.LoginRequest;
import com.interview.tracker.dto.RegisterRequest;
import com.interview.tracker.dto.SetPasswordRequest;
import com.interview.tracker.entity.Panel;
import com.interview.tracker.entity.User;
import com.interview.tracker.repository.PanelRepository;
import com.interview.tracker.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private EmailService emailService;
    private PanelRepository panelRepository;
    private JwtService jwtService;
    private UserService service;

    private String encryptPassword(String password) {
        byte[] utf8Bytes = password.getBytes(StandardCharsets.UTF_8);
        return Base64.getEncoder().encodeToString(utf8Bytes);
    }

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        emailService = mock(EmailService.class);
        panelRepository = mock(PanelRepository.class);
        jwtService = mock(JwtService.class);
        service = new UserService(userRepository, passwordEncoder, emailService, panelRepository, jwtService);
    }

    @Test
    void register_withValidRequest_savesUserAndSendsVerification() {
        RegisterRequest request = new RegisterRequest();
        request.setName("Hari");
        request.setEmail("hari@example.com");
        request.setPassword(encryptPassword("Secret@123"));
        request.setRole("CANDIDATE");
        request.setPhone("9876543210");

        when(userRepository.findByEmail("hari@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByPhone("9876543210")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("Secret@123")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(11L);
            return user;
        });

        AuthResponse response = service.register(request);

        assertEquals("CANDIDATE", response.getRole());
        assertEquals(11L, response.getUserId());
        verify(emailService).sendVerificationAndPasswordEmail(any(User.class));
    }

    @Test
    void register_withWeakPassword_rejects() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("hari@example.com");
        request.setPassword(encryptPassword("weak"));
        request.setRole("CANDIDATE");
        request.setPhone("9876543210");
        when(userRepository.findByEmail("hari@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByPhone("9876543210")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.register(request));

        assertTrue(ex.getMessage().contains("Password must be at least 8 characters"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_rejectsDuplicateEmailPhoneAndInvalidRole() {
        RegisterRequest request = registerRequest("hari@example.com", "9876543210", "CANDIDATE");
        when(userRepository.findByEmail("hari@example.com")).thenReturn(Optional.of(new User()));
        assertEquals("Email already exists", assertThrows(IllegalArgumentException.class,
                () -> service.register(request)).getMessage());

        reset(userRepository);
        when(userRepository.findByEmail("hari@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByPhone("9876543210")).thenReturn(Optional.of(new User()));
        assertEquals("Phone already exists", assertThrows(IllegalArgumentException.class,
                () -> service.register(request)).getMessage());

        reset(userRepository);
        request.setRole("ADMIN");
        when(userRepository.findByEmail("hari@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByPhone("9876543210")).thenReturn(Optional.empty());
        assertEquals("Invalid role", assertThrows(IllegalArgumentException.class,
                () -> service.register(request)).getMessage());
    }

    @Test
    void register_withoutPassword_savesPendingPasswordUser() {
        RegisterRequest request = registerRequest("nopass@example.com", "9876543211", "CANDIDATE");
        request.setPassword(encryptPassword(""));
        when(userRepository.findByEmail("nopass@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByPhone("9876543211")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(12L);
            return user;
        });

        AuthResponse response = service.register(request);

        assertEquals(12L, response.getUserId());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void login_forPanelCreatesPanelWhenMissingAndReturnsToken() {
        User user = verifiedUser("panel@example.com", "PANEL");
        user.setPassword("encoded");
        Panel panel = new Panel();
        panel.setId(5L);

        LoginRequest request = new LoginRequest();
        request.setEmail("panel@example.com");
        request.setPassword(encryptPassword("Secret@123"));

        when(userRepository.findByEmail("panel@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Secret@123", "encoded")).thenReturn(true);
        when(panelRepository.findByEmail("panel@example.com")).thenReturn(Optional.empty());
        when(panelRepository.save(any(Panel.class))).thenReturn(panel);
        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        AuthResponse response = service.login(request);

        assertEquals("Login successful", response.getMessage());
        assertEquals(5L, response.getPanelId());
        assertEquals("jwt-token", response.getToken());
    }

    @Test
    void login_rejectsMissingUnverifiedUnsetAndInvalidPasswords() {
        LoginRequest request = new LoginRequest();
        request.setEmail("user@example.com");
        request.setPassword(encryptPassword("Secret@123"));
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.empty());
        assertEquals("User not found", assertThrows(IllegalArgumentException.class,
                () -> service.login(request)).getMessage());

        User unverified = new User();
        unverified.setId(1L);
        unverified.setEmail("user@example.com");
        unverified.setEmailVerified(false);
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(unverified));
        assertTrue(assertThrows(IllegalArgumentException.class, () -> service.login(request))
                .getMessage().contains("Please verify your email first"));

        User unset = verifiedUser("user@example.com", "CANDIDATE");
        unset.setPassword("");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(unset));
        assertTrue(assertThrows(IllegalArgumentException.class, () -> service.login(request))
                .getMessage().contains("Password not set"));

        User invalid = verifiedUser("user@example.com", "CANDIDATE");
        invalid.setPassword("encoded");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(invalid));
        when(passwordEncoder.matches("Secret@123", "encoded")).thenReturn(false);
        assertEquals("Invalid password", assertThrows(IllegalArgumentException.class,
                () -> service.login(request)).getMessage());
    }

    @Test
    void login_withPlaintextPasswordUpgradesAndReturnsCandidateToken() {
        User user = verifiedUser("user@example.com", "CANDIDATE");
        user.setPassword("Secret@123");
        LoginRequest request = new LoginRequest();
        request.setEmail("user@example.com");
        request.setPassword(encryptPassword("Secret@123"));
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Secret@123", "Secret@123")).thenReturn(false);
        when(passwordEncoder.encode("Secret@123")).thenReturn("encoded");
        when(jwtService.generateToken(user)).thenReturn("jwt");

        AuthResponse response = service.login(request);

        assertEquals("jwt", response.getToken());
        assertEquals("encoded", user.getPassword());
        verify(userRepository).save(user);
    }

    @Test
    void setPassword_withExpiredToken_rejects() {
        User user = verifiedUser("user@example.com", "CANDIDATE");
        user.setVerificationToken("expired");
        user.setTokenExpiry(LocalDate.now().minusDays(1));
        SetPasswordRequest request = new SetPasswordRequest();
        request.setToken("expired");
        request.setNewPassword("Secret@123");
        when(userRepository.findAll()).thenReturn(List.of(user));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.setPassword(request));

        assertEquals("Token has expired. Please request a new password reset.", ex.getMessage());
    }

    @Test
    void verifyAndPasswordFlows_coverSuccessAndFailureBranches() {
        User user = verifiedUser("user@example.com", "PANEL");
        user.setVerificationToken("token");
        user.setTokenExpiry(LocalDate.now().plusDays(1));
        when(userRepository.findAll()).thenReturn(List.of(user));
        when(userRepository.save(user)).thenReturn(user);

        AuthResponse verified = service.verifyEmail("token");

        assertTrue(user.isEmailVerified());
        assertNull(user.getVerificationToken());
        assertEquals("PANEL", verified.getRole());
        verify(emailService).sendPasswordSetEmail(user);

        User setup = verifiedUser("setup@example.com", "CANDIDATE");
        setup.setVerificationToken("setup");
        setup.setTokenExpiry(LocalDate.now().plusDays(1));
        when(userRepository.findAll()).thenReturn(List.of(setup));
        assertEquals("setup@example.com", service.verifyAndPreparePasswordSetup("setup").get("email"));

        User panel = verifiedUser("panel@example.com", "PANEL");
        panel.setVerificationToken("set");
        panel.setTokenExpiry(LocalDate.now().plusDays(1));
        SetPasswordRequest request = new SetPasswordRequest();
        request.setToken("set");
        request.setNewPassword(encryptPassword("Secret@123"));
        when(userRepository.findAll()).thenReturn(List.of(panel));
        when(passwordEncoder.encode("Secret@123")).thenReturn("encoded");
        AuthResponse response = service.setPassword(request);
        assertEquals("PANEL", response.getRole());
        assertNotNull(panel.getActivatedAt());

        when(userRepository.findAll()).thenReturn(List.of());
        assertEquals("Invalid or expired verification token", assertThrows(IllegalArgumentException.class,
                () -> service.verifyEmail("bad")).getMessage());
        assertEquals("Invalid or expired token", assertThrows(IllegalArgumentException.class,
                () -> service.setPassword(request)).getMessage());

        User expiredVerify = verifiedUser("expired@example.com", "CANDIDATE");
        expiredVerify.setVerificationToken("expired");
        expiredVerify.setTokenExpiry(LocalDate.now().minusDays(1));
        when(userRepository.findAll()).thenReturn(List.of(expiredVerify));
        assertEquals("Verification token has expired", assertThrows(IllegalArgumentException.class,
                () -> service.verifyEmail("expired")).getMessage());

        User expiredSetup = verifiedUser("expired-setup@example.com", "CANDIDATE");
        expiredSetup.setVerificationToken("expired-setup");
        expiredSetup.setTokenExpiry(LocalDate.now().minusDays(1));
        when(userRepository.findAll()).thenReturn(List.of(expiredSetup));
        assertEquals("Verification token has expired", assertThrows(IllegalArgumentException.class,
                () -> service.verifyAndPreparePasswordSetup("expired-setup")).getMessage());
    }

    @Test
    void passwordResetAndResendVerification_coverBranches() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());
        assertEquals("Email not found", assertThrows(IllegalArgumentException.class,
                () -> service.requestPasswordReset("missing@example.com")).getMessage());

        User unverified = new User();
        unverified.setEmail("user@example.com");
        unverified.setEmailVerified(false);
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(unverified));
        assertEquals("Please verify your email first", assertThrows(IllegalArgumentException.class,
                () -> service.requestPasswordReset("user@example.com")).getMessage());

        User verified = verifiedUser("verified@example.com", "CANDIDATE");
        when(userRepository.findByEmail("verified@example.com")).thenReturn(Optional.of(verified));
        assertEquals("Password reset link sent to your email!", service.requestPasswordReset("verified@example.com").getMessage());

        verified.setPassword("encoded");
        assertEquals("Account is already verified and active", assertThrows(IllegalArgumentException.class,
                () -> service.resendVerificationEmail("verified@example.com")).getMessage());

        User pending = new User();
        pending.setId(3L);
        pending.setName("Pending");
        pending.setEmail("pending@example.com");
        pending.setRole("CANDIDATE");
        pending.setEmailVerified(false);
        when(userRepository.findByEmail("pending@example.com")).thenReturn(Optional.of(pending));
        assertEquals("Verification email resent successfully! Please check your inbox.",
                service.resendVerificationEmail("pending@example.com").getMessage());
    }

    @Test
    void createTestUser_withPanelRole_createsMatchingPanel() {
        CreateTestUserRequest request = new CreateTestUserRequest();
        request.setName("Panel User");
        request.setEmail("panel@example.com");
        request.setPassword(encryptPassword("Secret@123"));
        request.setRole("PANEL");
        when(userRepository.findByEmail("panel@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("Secret@123")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuthResponse response = service.createTestUser(request);

        assertEquals("PANEL", response.getRole());
        verify(panelRepository).save(any(Panel.class));
    }

    @Test
    void createTestUser_rejectsDuplicatesAndInvalidRolesAndSkipsPanelForCandidate() {
        CreateTestUserRequest request = new CreateTestUserRequest();
        request.setName("Candidate");
        request.setEmail("candidate@example.com");
        request.setPassword(encryptPassword("Secret@123"));
        request.setRole("CANDIDATE");
        when(userRepository.findByEmail("candidate@example.com")).thenReturn(Optional.of(new User()));
        assertEquals("Email already exists", assertThrows(IllegalArgumentException.class,
                () -> service.createTestUser(request)).getMessage());

        when(userRepository.findByEmail("candidate@example.com")).thenReturn(Optional.empty());
        request.setRole("ADMIN");
        assertTrue(assertThrows(IllegalArgumentException.class, () -> service.createTestUser(request))
                .getMessage().contains("Invalid role"));

        request.setRole("CANDIDATE");
        when(passwordEncoder.encode("Secret@123")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        AuthResponse response = service.createTestUser(request);
        assertEquals("CANDIDATE", response.getRole());
        verify(panelRepository, never()).save(any());
    }

    @Test
    void findHelpersDelegateToRepository() {
        User user = verifiedUser("user@example.com", "CANDIDATE");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(userRepository.findById(3L)).thenReturn(Optional.empty());

        assertEquals(Optional.of(user), service.findByEmail("user@example.com"));
        assertEquals(user, service.findById(2L));
        assertNull(service.findById(3L));
    }

    private RegisterRequest registerRequest(String email, String phone, String role) {
        RegisterRequest request = new RegisterRequest();
        request.setName("Hari");
        request.setEmail(email);
        request.setPassword("Secret@123");
        request.setRole(role);
        request.setPhone(phone);
        return request;
    }

    private User verifiedUser(String email, String role) {
        User user = new User();
        user.setId(2L);
        user.setName("Test User");
        user.setEmail(email);
        user.setRole(role);
        user.setEmailVerified(true);
        return user;
    }
}
