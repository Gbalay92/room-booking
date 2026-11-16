package com.gonzalobalay.roombooking.infrastructure.config;

import com.gonzalobalay.roombooking.application.port.UserRepository;
import com.gonzalobalay.roombooking.domain.Role;
import com.gonzalobalay.roombooking.domain.User;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Profile("dev")
class DevUserSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    DevUserSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        createIfMissing("admin@roombooking.local", "admin123", Role.ADMIN);
        createIfMissing("user@roombooking.local", "user123", Role.USER);
    }

    private void createIfMissing(String email, String rawPassword, Role role) {
        if (userRepository.findByEmail(email).isPresent()) {
            return;
        }
        userRepository.save(new User(UUID.randomUUID(), email, passwordEncoder.encode(rawPassword), role));
    }
}