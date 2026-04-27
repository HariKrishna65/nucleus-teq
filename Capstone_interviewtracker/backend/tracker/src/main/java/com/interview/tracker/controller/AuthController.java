package com.interview.tracker.controller;

import com.interview.tracker.dto.LoginRequest;
import com.interview.tracker.dto.RegisterRequest;
import com.interview.tracker.dto.AuthResponse;
import com.interview.tracker.service.UserService;
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
    public AuthResponse register(@RequestBody RegisterRequest request) {
        return userService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        return userService.login(request);
    }
}