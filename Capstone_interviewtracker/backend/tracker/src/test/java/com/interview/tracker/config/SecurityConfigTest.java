package com.interview.tracker.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SecurityConfigTest {

    @Test
    void passwordEncoder_usesBCrypt() {
        assertInstanceOf(BCryptPasswordEncoder.class, new SecurityConfig().passwordEncoder());
    }

    @Test
    void jwtAuthenticationConverter_readsRoleClaimAsRoleAuthority() {
        JwtAuthenticationConverter converter = new SecurityConfig().jwtAuthenticationConverter();
        Jwt jwt = new Jwt("token", null, null, Map.of("alg", "none"), Map.of("role", "HR", "sub", "hr@example.com"));

        var authentication = converter.convert(jwt);

        assertNotNull(authentication);
        assertTrue(authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_HR".equals(authority.getAuthority())));
    }
}
