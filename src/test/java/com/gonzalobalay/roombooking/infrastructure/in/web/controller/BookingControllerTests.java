package com.gonzalobalay.roombooking.infrastructure.in.web.controller;

import com.gonzalobalay.roombooking.application.port.BookingSummary;
import com.gonzalobalay.roombooking.application.port.PageResult;
import com.gonzalobalay.roombooking.application.service.BookingService;
import com.gonzalobalay.roombooking.application.service.RetryingBookingService;
import com.gonzalobalay.roombooking.domain.exception.InvalidTimeRangeException;
import com.gonzalobalay.roombooking.infrastructure.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

@WebMvcTest(BookingController.class)
@Import(SecurityConfig.class)
class BookingControllerTests {

    private static final UUID ROOM_ID = UUID.randomUUID();
    private static final Instant TEN = Instant.parse("2027-03-01T10:00:00Z");
    private static final Instant ELEVEN = Instant.parse("2027-03-01T11:00:00Z");
    private static final String BODY = """
            {"roomId":"%s","startsAt":"2027-03-01T10:00:00Z","endsAt":"2027-03-01T11:00:00Z"}
            """.formatted(ROOM_ID);

    @Autowired
    MockMvcTester mvc;

    @MockitoBean
    BookingService bookingService;
    @MockitoBean
    RetryingBookingService retryingBookingService;
    @MockitoBean
    JwtDecoder jwtDecoder;

    @Test
    void createBookingUsesTheTokenSubjectAsOwner() {
        given(retryingBookingService.createBooking(ROOM_ID, TEN, ELEVEN, "alice@test"))
                .willReturn(new BookingSummary(UUID.randomUUID(), ROOM_ID, "Room A", TEN, ELEVEN, "alice@test"));

        assertThat(mvc.post().uri("/api/bookings").with(user("alice@test"))
                .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .hasStatus(HttpStatus.CREATED)
                .headers().containsHeader("Location");

        then(retryingBookingService).should().createBooking(ROOM_ID, TEN, ELEVEN, "alice@test");
    }

    @Test
    void userListsOnlyTheirOwnBookings() {
        given(bookingService.findBookingSummariesByUser("alice@test", 0, 20))
                .willReturn(new PageResult<>(List.of(), 0, 20, 0));

        assertThat(mvc.get().uri("/api/bookings").with(user("alice@test"))).hasStatusOk();

        then(bookingService).should(never()).findAllBookingSummaries(any(Integer.class), any(Integer.class));
    }

    @Test
    void adminListsAllBookings() {
        given(bookingService.findAllBookingSummaries(0, 20)).willReturn(new PageResult<>(List.of(), 0, 20, 0));

        assertThat(mvc.get().uri("/api/bookings").with(admin("admin@test"))).hasStatusOk();

        then(bookingService).should().findAllBookingSummaries(0, 20);
    }

    @Test
    void returns401WithoutToken() {
        assertThat(mvc.get().uri("/api/bookings")).hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void returns400WhenEndsAtIsMissing() {
        assertThat(mvc.post().uri("/api/bookings").with(user("alice@test"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"roomId":"%s","startsAt":"2027-03-01T10:00:00Z"}
                        """.formatted(ROOM_ID)))
                .hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    void returns400WhenTimeRangeIsInverted() {
        given(retryingBookingService.createBooking(any(), any(), any(), any()))
                .willThrow(new InvalidTimeRangeException(ELEVEN, TEN));

        assertThat(mvc.post().uri("/api/bookings").with(user("alice@test"))
                .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .hasContentType(MediaType.APPLICATION_PROBLEM_JSON);
    }

    private static JwtRequestPostProcessor user(String email) {
        return jwt().jwt(token -> token.subject(email)).authorities(new SimpleGrantedAuthority("ROLE_USER"));
    }

    private static JwtRequestPostProcessor admin(String email) {
        return jwt().jwt(token -> token.subject(email)).authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }
}