package com.interview.tracker.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class DtoValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void loginRequest_withBlankValues_hasExpectedMessages() {
        LoginRequest request = new LoginRequest();
        request.setEmail("");
        request.setPassword("");

        Set<String> messages = messages(request);

        assertTrue(messages.contains("Email is required"));
        assertTrue(messages.contains("Password is required"));
    }

    @Test
    void registerRequest_withInvalidValues_hasExpectedMessages() {
        RegisterRequest request = new RegisterRequest();
        request.setName("A");
        request.setEmail("invalid");
        request.setRole("");
        request.setPassword("weak");
        request.setPhone("1");

        Set<String> messages = messages(request);

        assertTrue(messages.contains("Name must be between 2 and 80 characters"));
        assertTrue(messages.contains("Enter a valid email address"));
        assertTrue(messages.contains("Role is required"));
        assertTrue(messages.contains("Password must be at least 8 characters and include uppercase, lowercase, number, and special character."));
        assertTrue(messages.contains("Enter a valid phone number"));
    }

    @Test
    void setPasswordRequest_withWeakPassword_hasPolicyMessage() {
        SetPasswordRequest request = new SetPasswordRequest();
        request.setToken("token");
        request.setNewPassword("weak");

        assertTrue(messages(request).contains(
                "Password must be at least 8 characters and include uppercase, lowercase, number, and special character."
        ));
    }

    @Test
    void assignPanelRequest_withoutEmails_hasExpectedMessage() {
        AssignPanelRequest request = new AssignPanelRequest();

        assertTrue(messages(request).contains("At least one panel email is required"));
    }

    @Test
    void createPanelRequest_withInvalidEmailAndMissingFields_hasExpectedMessages() {
        CreatePanelRequest request = new CreatePanelRequest();
        request.setName("A");
        request.setEmail("invalid");
        request.setPhone("1");

        Set<String> messages = messages(request);

        assertTrue(messages.contains("Name must be between 2 and 80 characters"));
        assertTrue(messages.contains("Enter a valid panel email"));
        assertTrue(messages.contains("Enter a valid panel phone number"));
        assertTrue(messages.contains("Organization is required"));
        assertTrue(messages.contains("Designation is required"));
        assertTrue(messages.contains("Expertise is required"));
    }

    @Test
    void referralCandidateRequest_withMissingJobAndNegativeExperience_hasExpectedMessages() {
        ReferralCandidateRequest request = new ReferralCandidateRequest();
        request.setName("");
        request.setEmail("bad");
        request.setPhone("1");
        request.setExperience(-1.0);

        Set<String> messages = messages(request);

        assertTrue(messages.contains("Name is required"));
        assertTrue(messages.contains("Enter a valid email address"));
        assertTrue(messages.contains("Enter a valid phone number"));
        assertTrue(messages.contains("Job description is required"));
        assertTrue(messages.contains("Experience cannot be negative"));
    }

    @Test
    void dtoBeanAccessorsRoundTripCommonPropertyTypes() throws Exception {
        for (Class<?> type : List.of(
                AssignPanelRequest.class,
                AuthResponse.class,
                CreatePanelRequest.class,
                CreateTestUserRequest.class,
                EmailRequest.class,
                LoginRequest.class,
                ReferralCandidateRequest.class,
                RegisterRequest.class,
                SetPasswordRequest.class,
                VerifyEmailRequest.class
        )) {
            Object bean = type.getConstructor().newInstance();
            for (PropertyDescriptor property : Introspector.getBeanInfo(type, Object.class).getPropertyDescriptors()) {
                if (property.getReadMethod() == null || property.getWriteMethod() == null) {
                    continue;
                }
                Object value = valueFor(property.getPropertyType());
                if (value == null) {
                    continue;
                }

                property.getWriteMethod().invoke(bean, value);
                assertEquals(value, property.getReadMethod().invoke(bean));
            }
        }
    }

    @Test
    void authResponseConstructorsPopulateExpectedFields() {
        AuthResponse minimal = new AuthResponse("ok", "HR", 1L);
        AuthResponse withPanel = new AuthResponse("ok", "PANEL", 2L, "Panel", 9L);
        AuthResponse withToken = new AuthResponse("ok", "PANEL", 2L, "Panel", 9L, "jwt");

        assertEquals("ok", minimal.getMessage());
        assertEquals("HR", minimal.getRole());
        assertEquals(1L, minimal.getUserId());
        assertEquals("Panel", withPanel.getName());
        assertEquals(9L, withPanel.getPanelId());
        assertEquals("jwt", withToken.getToken());
    }

    private Set<String> messages(Object request) {
        return validator.validate(request).stream()
                .map(violation -> violation.getMessage())
                .collect(Collectors.toSet());
    }

    private Object valueFor(Class<?> type) {
        if (type == String.class) return "value";
        if (type == Long.class) return 1L;
        if (type == Integer.class) return 30;
        if (type == Double.class) return 2.5;
        if (type == LocalDate.class) return LocalDate.of(2026, 5, 5);
        if (type == LocalDateTime.class) return LocalDateTime.of(2026, 5, 5, 9, 30);
        if (type == List.class) return new ArrayList<String>(List.of("panel@example.com"));
        return null;
    }
}
