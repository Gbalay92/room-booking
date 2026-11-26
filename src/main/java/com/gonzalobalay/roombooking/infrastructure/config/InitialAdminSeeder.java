package com.gonzalobalay.roombooking.infrastructure.config;

import com.gonzalobalay.roombooking.application.port.UserRepository;
import com.gonzalobalay.roombooking.domain.Role;
import com.gonzalobalay.roombooking.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.UUID;


@Component
@Profile("prod")
class InitialAdminSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(InitialAdminSeeder.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String email;
    private final String password;

    InitialAdminSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       @Value("${bootstrap.admin.email:}") String email,
                       @Value("${bootstrap.admin.password:}") String password) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.email = email;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (email.isBlank() || password.isBlank()) {
            log.warn("No bootstrap admin configured (ADMIN_EMAIL / ADMIN_PASSWORD)");
            return;
        }
        if (userRepository.findByEmail(email).isPresent()) {
            return;
        }
        userRepository.save(new User(UUID.randomUUID(), email, passwordEncoder.encode(password), Role.ADMIN));
        log.info("Bootstrap admin {} created", email);
    }
}