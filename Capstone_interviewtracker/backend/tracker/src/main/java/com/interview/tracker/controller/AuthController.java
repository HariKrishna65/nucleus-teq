package com.interview.tracker.controller;

import com.interview.tracker.dto.LoginRequest;
import com.interview.tracker.dto.RegisterRequest;
import com.interview.tracker.dto.AuthResponse;
import com.interview.tracker.dto.SetPasswordRequest;
import com.interview.tracker.dto.CreateTestUserRequest;
import com.interview.tracker.dto.VerifyEmailRequest;
import com.interview.tracker.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import static com.interview.tracker.constants.AppConstants.AUTH;

@RestController
@RequestMapping(AUTH)

public class AuthController {

    @Autowired
    private UserService userService;
    
    @CrossOrigin(origins = "*")

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return userService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return userService.login(request);
    }

    @PostMapping("/verify")
    public AuthResponse verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        return userService.verifyEmail(request.getToken());
    }

    @PostMapping("/set-password")
    public AuthResponse setPassword(@Valid @RequestBody SetPasswordRequest request) {
        return userService.setPassword(request);
    }

    @PostMapping("/forgot-password")
    public AuthResponse forgotPassword(@Valid @RequestBody LoginRequest request) {
        return userService.requestPasswordReset(request.getEmail());
    }

    // Test endpoint - create user directly with password (bypasses email verification)
    // WARNING: This endpoint is open for testing purposes only
    @PostMapping("/create-test-user")
    public AuthResponse createTestUser(@Valid @RequestBody CreateTestUserRequest request) {
        return userService.createTestUser(request);
    }
}