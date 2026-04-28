package com.interview.tracker.service;

import com.interview.tracker.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class JwtServiceTest {

    @Autowired
    private JwtService jwtService;

    @Test
    void generateToken_containsJwtSegments() {
        User u = new User();
        u.setId(1L);
        u.setEmail("a@b.com");
        u.setRole("HR");
        String token = jwtService.generateToken(u);
        assertNotNull(token);
        assertTrue(token.split("\\.").length >= 3);
    }
}

