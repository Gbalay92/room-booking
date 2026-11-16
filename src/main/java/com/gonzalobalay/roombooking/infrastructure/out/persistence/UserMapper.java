package com.gonzalobalay.roombooking.infrastructure.out.persistence;

import com.gonzalobalay.roombooking.domain.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    User toDomain(UserJpaEntity entity) {
        return new User(entity.getId(), entity.getEmail(), entity.getPasswordHash(), entity.getRole());
    }

    UserJpaEntity toJpaEntity(User domain) {
        return new UserJpaEntity(domain.id(), domain.email(), domain.passwordHash(), domain.role());
    }

}
