package com.interview.tracker.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SecurityUtilTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void currentValues_withoutAuthentication_returnNull() {
        assertNull(SecurityUtil.currentUserId());
        assertNull(SecurityUtil.currentEmail());
        assertNull(SecurityUtil.currentRole());
    }

    @Test
    void currentValues_fromJwtClaims_areExtracted() {
        Jwt jwt = new Jwt("token", null, null, Map.of("alg", "none"), Map.of(
                "sub", "user@example.com",
                "userId", 7,
                "role", "HR"
        ));
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));

        assertEquals(7L, SecurityUtil.currentUserId());
        assertEquals("user@example.com", SecurityUtil.currentEmail());
        assertEquals("HR", SecurityUtil.currentRole());
    }
}
