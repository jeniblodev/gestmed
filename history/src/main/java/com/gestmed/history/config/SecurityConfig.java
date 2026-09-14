package com.gestmed.history.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/graphiql/**").authenticated()
                        .requestMatchers("/graphql").authenticated()
                        .anyRequest().denyAll()
                )
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(
            PasswordEncoder passwordEncoder) {

        var doctor = User.builder()
                .username("doctor")
                .password(passwordEncoder.encode("doctor123"))
                .roles("DOCTOR")
                .build();

        var nurse = User.builder()
                .username("nurse")
                .password(passwordEncoder.encode("nurse123"))
                .roles("NURSE")
                .build();

        var patient = User.builder()
                .username("patient")
                .password(passwordEncoder.encode("patient123"))
                .roles("PATIENT")
                .build();

        return new InMemoryUserDetailsManager(
                doctor,
                nurse,
                patient
        );
    }
}