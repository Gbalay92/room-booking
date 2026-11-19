package com.gonzalobalay.roombooking;

import com.gonzalobalay.roombooking.application.port.UserRepository;
import com.gonzalobalay.roombooking.domain.Role;
import com.gonzalobalay.roombooking.domain.User;
import com.gonzalobalay.roombooking.infrastructure.in.web.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.time.Instant;
import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
class BookingFlowTests {

    private static final String ADMIN = "admin@flow.test";
    private static final String ALICE = "alice@flow.test";
    private static final String BOB = "bob@flow.test";
    private static final String PASSWORD = "secret123";
    private static final Instant TEN = Instant.parse("2027-03-01T10:00:00Z");
    private static final Instant ELEVEN = Instant.parse("2027-03-01T11:00:00Z");

    @LocalServerPort
    int port;
    @Autowired
    UserRepository userRepository;
    @Autowired
    PasswordEncoder passwordEncoder;

    RestTestClient client;

    @BeforeEach
    void setUp() {
        client = RestTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
        createUserIfMissing(ADMIN, Role.ADMIN);
        createUserIfMissing(ALICE, Role.USER);
        createUserIfMissing(BOB, Role.USER);
    }

    @Test
    void userLogsInAndBooksARoom() {
        UUID roomId = createRoom();

        client.post().uri("/api/bookings")
                .headers(h -> h.setBearerAuth(token(ALICE)))
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CreateBookingRequest(roomId, TEN, ELEVEN))
                .exchange()
                .expectStatus().isCreated()
                .expectBody().jsonPath("$.userName").isEqualTo(ALICE);
    }

    @Test
    void overlappingBookingReturns409() {
        UUID roomId = createRoom();
        book(ALICE, roomId);

        client.post().uri("/api/bookings")
                .headers(h -> h.setBearerAuth(token(BOB)))
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CreateBookingRequest(roomId, TEN.plusSeconds(1800), ELEVEN.plusSeconds(1800)))
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON);
    }

    @Test
    void userCannotCancelSomeoneElsesBooking() {
        UUID bookingId = book(ALICE, createRoom());

        client.delete().uri("/api/bookings/{id}", bookingId)
                .headers(h -> h.setBearerAuth(token(BOB)))
                .exchange()
                .expectStatus().isForbidden();

        client.get().uri("/api/bookings/{id}", bookingId)
                .headers(h -> h.setBearerAuth(token(ALICE)))
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void requestWithoutTokenReturns401() {
        client.get().uri("/api/bookings")
                .exchange()
                .expectStatus().isUnauthorized()
                .expectHeader().exists("WWW-Authenticate")
                .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON);
    }

    private String token(String email) {
        return client.post().uri("/auth/token")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new TokenRequest(email, PASSWORD))
                .exchange()
                .expectStatus().isOk()
                .expectBody(TokenResponse.class).returnResult().getResponseBody().accessToken();
    }

    private UUID createRoom() {
        return client.post().uri("/api/rooms")
                .headers(h -> h.setBearerAuth(token(ADMIN)))
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CreateRoomRequest("Flow room", 4))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(RoomResponse.class).returnResult().getResponseBody().id();
    }

    private UUID book(String email, UUID roomId) {
        return client.post().uri("/api/bookings")
                .headers(h -> h.setBearerAuth(token(email)))
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CreateBookingRequest(roomId, TEN, ELEVEN))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(BookingResponse.class).returnResult().getResponseBody().id();
    }

    private void createUserIfMissing(String email, Role role) {
        if (userRepository.findByEmail(email).isEmpty()) {
            userRepository.save(new User(UUID.randomUUID(), email, passwordEncoder.encode(PASSWORD), role));
        }
    }
}