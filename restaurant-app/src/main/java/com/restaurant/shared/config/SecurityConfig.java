package com.restaurant.shared.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${security.admin.password}")   private String adminPassword;
    @Value("${security.kitchen.password}") private String kitchenPassword;
    @Value("${security.student.password}") private String studentPassword;
    @Value("${security.teacher.password}") private String teacherPassword;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors(Customizer.withDefaults())
            .csrf(csrf -> csrf.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/index.html", "/admin.html", "/kitchen.html",
                        "/uploads/**", "/ws/**").permitAll()
                .requestMatchers("/api/**", "/orders/**", "/wallets/**", "/pickups/**").permitAll()
                .anyRequest().permitAll())
            .httpBasic(Customizer.withDefaults());
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder encoder) {
        return new InMemoryUserDetailsManager(
            User.builder().username("admin_user")
                .password(encoder.encode(adminPassword)).roles("ADMIN_USER").build(),
            User.builder().username("kitchen_user")
                .password(encoder.encode(kitchenPassword)).roles("KITCHEN_USER").build(),
            User.builder().username("student")
                .password(encoder.encode(studentPassword)).roles("STUDENT").build(),
            User.builder().username("teacher")
                .password(encoder.encode(teacherPassword)).roles("TEACHER").build()
        );
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
