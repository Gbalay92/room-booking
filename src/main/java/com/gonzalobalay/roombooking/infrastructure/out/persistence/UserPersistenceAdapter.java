package com.gonzalobalay.roombooking.infrastructure.out.persistence;

import com.gonzalobalay.roombooking.application.port.UserRepository;
import com.gonzalobalay.roombooking.domain.User;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public class UserPersistenceAdapter implements UserRepository {

    private final JpaUserRepository userJpaRepository;
    private final UserMapper userMapper;

    public UserPersistenceAdapter(JpaUserRepository userJpaRepository, UserMapper userMapper) {
        this.userJpaRepository = userJpaRepository;
        this.userMapper = userMapper;
    }

    @Override
    public User findByUsername(String username) {
        return null;
    }

    @Override
    public User save(User user) {
        UserJpaEntity saved = userJpaRepository.saveAndFlush(userMapper.toJpaEntity(user));
        return userMapper.toDomain(saved);
    }

    @Override
    public void deleteById(Long id) {

    }

    @Override
    public void delete(User user) {

    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userJpaRepository.findByEmailIgnoreCase(email).map(userMapper::toDomain);
    }
}
