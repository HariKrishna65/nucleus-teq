package com.interview.tracker.service;

import com.interview.tracker.entity.User;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtServiceTest {

    @Test
    void generateToken_buildsClaimsAndReturnsEncodedToken() {
        JwtEncoder jwtEncoder = mock(JwtEncoder.class);
        JwtService jwtService = new JwtService(jwtEncoder);
        ReflectionTestUtils.setField(jwtService, "issuer", "interview-tracker-test");
        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(
                new Jwt("header.payload.signature", null, null, java.util.Map.of("alg", "HS256"), java.util.Map.of("sub", "a@b.com"))
        );

        User u = new User();
        u.setId(1L);
        u.setEmail("a@b.com");
        u.setRole("HR");

        String token = jwtService.generateToken(u);

        assertEquals("header.payload.signature", token);
        ArgumentCaptor<JwtEncoderParameters> captor = ArgumentCaptor.forClass(JwtEncoderParameters.class);
        verify(jwtEncoder).encode(captor.capture());
        JwtClaimsSet claims = captor.getValue().getClaims();
        assertEquals("a@b.com", claims.getSubject());
        assertEquals("interview-tracker-test", claims.getClaim("iss"));
        assertEquals("HR", claims.getClaim("role"));
        assertEquals(Long.valueOf(1L), claims.getClaim("userId"));
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiresAt());
    }
}
