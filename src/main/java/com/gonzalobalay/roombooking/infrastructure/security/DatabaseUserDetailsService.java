package com.gonzalobalay.roombooking.infrastructure.security;

import com.gonzalobalay.roombooking.application.port.UserRepository;
import com.gonzalobalay.roombooking.domain.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
class DatabaseUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    DatabaseUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("..."));
        return org.springframework.security.core.userdetails.User
                .withUsername(user.email())
                .password(user.passwordHash())
                .roles(user.role().name())
                .build();
    }
}