package com.example.canteen.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${security.admin.password}")
    private String adminPassword;

    @Value("${security.kitchen.password}")
    private String kitchenPassword;

    @Value("${security.student.password}")
    private String studentPassword;

    @Value("${security.teacher.password}")
    private String teacherPassword;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/index.html", "/admin.html", "/kitchen.html",
                                "/demo-role-board.html", "/demo/**", "/uploads/**").permitAll()
                        .requestMatchers("/api/**").permitAll()
                        .anyRequest().permitAll())
                .httpBasic(basic -> {});
        return http.build();
    }

    @Bean
    UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        UserDetails admin = User.builder()
                .username("admin_user")
                .password(passwordEncoder.encode(adminPassword))
                .roles("ADMIN_USER")
                .build();
        UserDetails kitchen = User.builder()
                .username("kitchen_user")
                .password(passwordEncoder.encode(kitchenPassword))
                .roles("KITCHEN_USER")
                .build();
        UserDetails student = User.builder()
                .username("student")
                .password(passwordEncoder.encode(studentPassword))
                .roles("STUDENT")
                .build();
        UserDetails teacher = User.builder()
                .username("teacher")
                .password(passwordEncoder.encode(teacherPassword))
                .roles("TEACHER")
                .build();
        return new InMemoryUserDetailsManager(admin, kitchen, student, teacher);
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
