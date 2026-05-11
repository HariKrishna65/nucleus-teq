package com.interview.tracker.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

public class SecurityUtil {
    private SecurityUtil() {}

    public static Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) return null;
        Object principal = auth.getPrincipal();
        if (principal instanceof Jwt jwt) {
            Object userId = jwt.getClaims().get("userId");
            if (userId instanceof Integer i) return i.longValue();
            if (userId instanceof Long l) return l;
        }
        return null;
    }

    public static String currentEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) return null;
        Object principal = auth.getPrincipal();
        if (principal instanceof Jwt jwt) {
            return jwt.getSubject();
        }
        return null;
    }

    public static String currentRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) return null;
        Object principal = auth.getPrincipal();
        if (principal instanceof Jwt jwt) {
            Object role = jwt.getClaims().get("role");
            return role == null ? null : String.valueOf(role);
        }
        return null;
    }
}

