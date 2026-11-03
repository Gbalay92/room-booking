package com.gonzalobalay.roombooking;

import com.gonzalobalay.roombooking.application.service.RoomService;
import com.gonzalobalay.roombooking.domain.exception.RoomCapacityExceededException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
class RoomBookingApplicationTests {

    @Autowired
    private RoomService roomService;

	@Test
	void contextLoads() {
	}

    @Test
    void createdAtUsesTheFixedClock() {
        assertThat(roomService.create("Room 1", 2).createdAt()).isEqualTo(Instant.parse("2026-01-01T10:00:00Z"));
    }

    @Test
    void rejectsCapacityAboveTheTestProfileLimit() {
        assertThrows(RoomCapacityExceededException.class, () -> roomService.create("Room 1", 49));
    }

}
