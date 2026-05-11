package com.interview.tracker.controller;

import com.interview.tracker.exception.GlobalExceptionHandler;
import com.interview.tracker.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static com.interview.tracker.constants.AppConstants.AUTH;
import static com.interview.tracker.constants.AppConstants.LOGIN;
import static com.interview.tracker.constants.AppConstants.SET_PASSWORD;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerValidationTest {

    private UserService userService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(new AuthController(userService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void login_withBlankEmailAndPassword_returnsFieldValidationErrors() throws Exception {
        mockMvc.perform(post(AUTH + LOGIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "",
                                  "password": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fields.email").value("Email is required"))
                .andExpect(jsonPath("$.fields.password").value("Password is required"));

        verifyNoInteractions(userService);
    }

    @Test
    void login_withInvalidEmail_returnsEmailValidationError() throws Exception {
        mockMvc.perform(post(AUTH + LOGIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "not-an-email",
                                  "password": "Secret@123"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fields.email").value("Enter a valid email address"));

        verifyNoInteractions(userService);
    }

    @Test
    void setPassword_withWeakPassword_returnsPasswordPolicyError() throws Exception {
        mockMvc.perform(post(AUTH + SET_PASSWORD)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "token": "setup-token",
                                  "newPassword": "weak"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fields.newPassword").value(
                        "Password must be at least 8 characters and include uppercase, lowercase, number, and special character."
                ));

        verifyNoInteractions(userService);
    }
}
