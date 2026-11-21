package com.gonzalobalay.roombooking;

import com.gonzalobalay.roombooking.application.service.RoomService;
import com.gonzalobalay.roombooking.domain.exception.RoomCapacityExceededException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
class RoomBookingApplicationTests {

    @Autowired
    private RoomService roomService;

	@Test
	void contextLoads() {
	}

}
