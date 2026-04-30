package com.interview.tracker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import static com.interview.tracker.constants.AppConstants.AUTH;
import static com.interview.tracker.constants.AppConstants.CANDIDATES;
import static com.interview.tracker.constants.AppConstants.CREATE_TEST_USER;
import static com.interview.tracker.constants.AppConstants.FEEDBACK;
import static com.interview.tracker.constants.AppConstants.FORGOT_PASSWORD;
import static com.interview.tracker.constants.AppConstants.HR;
import static com.interview.tracker.constants.AppConstants.INTERVIEWS;
import static com.interview.tracker.constants.AppConstants.JD;
import static com.interview.tracker.constants.AppConstants.LOGIN;
import static com.interview.tracker.constants.AppConstants.REGISTER;
import static com.interview.tracker.constants.AppConstants.RESEND_VERIFICATION;
import static com.interview.tracker.constants.AppConstants.SET_PASSWORD;
import static com.interview.tracker.constants.AppConstants.VERIFY;
import static com.interview.tracker.constants.AppConstants.VERIFY_AND_SET_PASSWORD;
import static com.interview.tracker.constants.AppConstants.ROLE_CANDIDATE;
import static com.interview.tracker.constants.AppConstants.ROLE_HR;
import static com.interview.tracker.constants.AppConstants.ROLE_PANEL;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            .cors(Customizer.withDefaults())
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers(
                        HttpMethod.POST,
                        AUTH + REGISTER,
                        AUTH + LOGIN,
                        AUTH + VERIFY,
                        AUTH + VERIFY_AND_SET_PASSWORD,
                        AUTH + SET_PASSWORD,
                        AUTH + FORGOT_PASSWORD,
                        AUTH + RESEND_VERIFICATION,
                        AUTH + CREATE_TEST_USER
                ).permitAll()

                .requestMatchers(HttpMethod.GET, JD, JD + "/*").permitAll()

                .requestMatchers(HR + "/**").hasRole(ROLE_HR)

                .requestMatchers(CANDIDATES + "/**").hasAnyRole(ROLE_CANDIDATE, ROLE_HR)

                .requestMatchers(HttpMethod.POST, INTERVIEWS).hasRole(ROLE_HR)
                .requestMatchers(INTERVIEWS + "/panel/**").hasRole(ROLE_HR)
                .requestMatchers(FEEDBACK + "/**").hasAnyRole(ROLE_PANEL, ROLE_HR)
                .requestMatchers(INTERVIEWS + "/**").hasAnyRole(ROLE_PANEL, ROLE_HR, ROLE_CANDIDATE)

                .requestMatchers(HttpMethod.POST, JD).hasRole(ROLE_HR)
                .requestMatchers(HttpMethod.DELETE, JD + "/*").hasRole(ROLE_HR)

                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));

        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthorityPrefix("ROLE_");
        authoritiesConverter.setAuthoritiesClaimName("role");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
        return converter;
    }
}