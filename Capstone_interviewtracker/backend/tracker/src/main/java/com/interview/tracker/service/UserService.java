package com.interview.tracker.service;

import com.interview.tracker.dto.LoginRequest;
import com.interview.tracker.dto.RegisterRequest;
import com.interview.tracker.dto.AuthResponse;
import com.interview.tracker.entity.User;
import com.interview.tracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final List<String> roles = List.of("HR", "PANEL", "CANDIDATE");

    // ✅ SIGNUP
    public AuthResponse register(RegisterRequest request) {

        // email validation
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }

        // role validation
        if (request.getRole() == null || !roles.contains(request.getRole())) {
            throw new IllegalArgumentException("Invalid role");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());

        // 🔐 encrypt password
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        user.setRole(request.getRole());

        User saved = userRepository.save(user);

        return new AuthResponse("User registered successfully", saved.getRole(), saved.getId());
    }

    // ✅ LOGIN
    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // 🔐 match encrypted password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid password");
        }

        return new AuthResponse("Login successful", user.getRole(), user.getId());
    }
}