package com.interview.tracker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public auth endpoints
                .requestMatchers(HttpMethod.POST, "/auth/register", "/auth/login", "/auth/verify", "/auth/set-password", "/auth/forgot-password").permitAll()

                // Public read-only endpoints (home page / listings)
                .requestMatchers(HttpMethod.GET, "/jd", "/jd/*").permitAll()

                // HR-only workflow
                .requestMatchers("/hr/**").hasRole("HR")

                // Candidate endpoints
                .requestMatchers("/candidates/**").hasAnyRole("CANDIDATE", "HR")

                // Panel endpoints
                .requestMatchers(HttpMethod.POST, "/interviews").hasRole("HR")
                .requestMatchers("/interviews/panel/**").hasRole("HR")
                .requestMatchers("/feedback/**").hasAnyRole("PANEL", "HR")
                .requestMatchers("/interviews/**").hasAnyRole("PANEL", "HR", "CANDIDATE")

                // JD creation should be HR-only
                .requestMatchers(HttpMethod.POST, "/jd").hasRole("HR")
                .requestMatchers(HttpMethod.DELETE, "/jd/*").hasRole("HR")

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