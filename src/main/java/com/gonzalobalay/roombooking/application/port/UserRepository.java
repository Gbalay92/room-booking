package com.gonzalobalay.roombooking.application.port;

import com.gonzalobalay.roombooking.domain.User;
import com.gonzalobalay.roombooking.infrastructure.out.persistence.UserJpaEntity;

import java.util.Optional;

public interface UserRepository {

    User findByUsername(String username);
    User save(User user);
    void deleteById(Long id);
    void delete(User user);
    Optional<User> findByEmail(String email);
}
