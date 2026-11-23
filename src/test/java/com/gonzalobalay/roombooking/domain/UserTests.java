package com.gonzalobalay.roombooking.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserTests {

    private static final UUID ID = UUID.randomUUID();
    private static final String EMAIL = "alice@test.com";
    private static final String PASSWORD_HASH = "hashedPassword";
    private static final Role ROLE = Role.USER;

    @Test
    void createsAValidUser() {
        assertThatNoException().isThrownBy(() -> new User(ID, EMAIL, PASSWORD_HASH, ROLE));
    }

    @Test
    void rejectsAnyNullField() {
        assertThatThrownBy(() -> new User(null, EMAIL, PASSWORD_HASH, ROLE))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new User(ID, null, PASSWORD_HASH, ROLE))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new User(ID, EMAIL, null, ROLE))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new User(ID, EMAIL, PASSWORD_HASH, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void rejectsBlankEmail(String email) {
        assertThatThrownBy(() -> new User(ID, email, PASSWORD_HASH, ROLE))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
