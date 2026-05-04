package com.interview.tracker.controller;

import com.interview.tracker.dto.LoginRequest;
import com.interview.tracker.dto.RegisterRequest;
import com.interview.tracker.dto.AuthResponse;
import com.interview.tracker.dto.EmailRequest;
import com.interview.tracker.dto.SetPasswordRequest;
import com.interview.tracker.dto.CreateTestUserRequest;
import com.interview.tracker.dto.VerifyEmailRequest;
import com.interview.tracker.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import static com.interview.tracker.constants.AppConstants.AUTH;
import static com.interview.tracker.constants.AppConstants.CREATE_TEST_USER;
import static com.interview.tracker.constants.AppConstants.FORGOT_PASSWORD;
import static com.interview.tracker.constants.AppConstants.LOGIN;
import static com.interview.tracker.constants.AppConstants.REGISTER;
import static com.interview.tracker.constants.AppConstants.RESEND_VERIFICATION;
import static com.interview.tracker.constants.AppConstants.SET_PASSWORD;
import static com.interview.tracker.constants.AppConstants.VERIFY;
import static com.interview.tracker.constants.AppConstants.VERIFY_AND_SET_PASSWORD;

@RestController
@RequestMapping(AUTH)

public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping(REGISTER)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration request received for email={} role={}", request.getEmail(), request.getRole());
        return userService.register(request);
    }

    @PostMapping(LOGIN)
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request received for email={}", request.getEmail());
        return userService.login(request);
    }

    @PostMapping(VERIFY)
    public AuthResponse verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        return userService.verifyEmail(request.getToken());
    }

    @PostMapping(VERIFY_AND_SET_PASSWORD)
    public java.util.Map<String, Object> verifyAndSetPassword(@Valid @RequestBody VerifyEmailRequest request) {
        return userService.verifyAndPreparePasswordSetup(request.getToken());
    }

    @PostMapping(SET_PASSWORD)
    public AuthResponse setPassword(@Valid @RequestBody SetPasswordRequest request) {
        return userService.setPassword(request);
    }

    @PostMapping(FORGOT_PASSWORD)
    public AuthResponse forgotPassword(@Valid @RequestBody EmailRequest request) {
        log.info("Password reset request received for email={}", request.getEmail());
        return userService.requestPasswordReset(request.getEmail());
    }

    // Test endpoint - create user directly with password (bypasses email verification)
    // WARNING: This endpoint is open for testing purposes only
    @PostMapping(CREATE_TEST_USER)
    public AuthResponse createTestUser(@Valid @RequestBody CreateTestUserRequest request) {
        return userService.createTestUser(request);
    }

    @PostMapping(RESEND_VERIFICATION)
    public AuthResponse resendVerificationEmail(@Valid @RequestBody EmailRequest request) {
        return userService.resendVerificationEmail(request.getEmail());
    }
}
