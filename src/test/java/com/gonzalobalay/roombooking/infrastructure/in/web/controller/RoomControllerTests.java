package com.gonzalobalay.roombooking.infrastructure.in.web.controller;

import com.gonzalobalay.roombooking.application.service.RoomService;
import com.gonzalobalay.roombooking.domain.Room;
import com.gonzalobalay.roombooking.domain.exception.RoomCapacityExceededException;
import com.gonzalobalay.roombooking.domain.exception.RoomNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import java.time.Clock;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@WebMvcTest(RoomController.class)
public class RoomControllerTests {

    @Autowired
    private MockMvcTester mvc;

    @MockitoBean
    private RoomService roomService;

    @Test
    void returns404WhenRoomDoesNotExist() {
        UUID id = UUID.randomUUID();
        given(roomService.findById(id)).willThrow(new RoomNotFoundException(id));

        assertThat(mvc.get().uri("/api/rooms/{id}", id))
                .hasStatus(HttpStatus.NOT_FOUND)
                .bodyJson()
                .extractingPath("$.title")
                .isEqualTo("Room not found");
    }

    @Test
    void returns422WhenCapacityExceeded() {
        given(roomService.create("Test Room", 21)).willThrow(new RoomCapacityExceededException(21, 20));
        assertThat(mvc.post().uri("/api/rooms").contentType(MediaType.APPLICATION_JSON)
                .content("""
                { "name": "Test Room", "capacity": 21 }
                """))
                .hasStatus(HttpStatus.UNPROCESSABLE_CONTENT)
                .bodyJson()
                .extractingPath("$.title")
                .isEqualTo("Room capacity exceeded");
    }

    @Test
    void createsRoomAndReturns201WithLocation() {
        Room room = new Room(UUID.randomUUID(), "Test Room", 8, Clock.systemUTC().instant());
        given(roomService.create("Test Room", 8)).willReturn(room);
        assertThat(mvc.post().uri("/api/rooms").contentType(MediaType.APPLICATION_JSON)
                .content("""
                { "name": "Test Room", "capacity": 8 }
                """))
                .hasStatus(HttpStatus.CREATED)
                .hasHeader("Location", "http://localhost/api/rooms/" + room.id());
    }

    @Test
    void returns400WithFieldErrorsWhenNameIsBlank() {
        assertThat(mvc.post().uri("/api/rooms").contentType(MediaType.APPLICATION_JSON)
                .content("""
                { "name": "", "capacity": 8 }
                """))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson()
                .extractingPath("$.errors[0].field")
                .isEqualTo("name");
        then(roomService).shouldHaveNoInteractions();

    }

    @Test
    void returns400WithFieldErrorsWhenNameIsOverSizeLimit() {
        String longName = "a".repeat(51);
        assertThat(mvc.post().uri("/api/rooms").contentType(MediaType.APPLICATION_JSON)
                .content(String.format("""
                { "name": "%s", "capacity": 8 }
                """, longName)))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson()
                .extractingPath("$.errors[0].field")
                .isEqualTo("name");
        then(roomService).shouldHaveNoInteractions();

    }

    @Test
    void returns400WithFieldErrorsWhenNameIsOverSizeLimitPut() {
        String longName = "a".repeat(51);
        assertThat(mvc.put().uri("/api/rooms/" + UUID.randomUUID()).contentType(MediaType.APPLICATION_JSON)
                .content(String.format("""
                { "name": "%s", "capacity": 8 }
                """, longName)))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson()
                .extractingPath("$.errors[0].field")
                .isEqualTo("name");
        then(roomService).shouldHaveNoInteractions();

    }
}
