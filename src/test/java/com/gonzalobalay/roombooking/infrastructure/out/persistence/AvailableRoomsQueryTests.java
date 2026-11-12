package com.gonzalobalay.roombooking.infrastructure.out.persistence;

import com.gonzalobalay.roombooking.application.port.AvailableRoom;
import com.gonzalobalay.roombooking.application.service.BookingService;
import com.gonzalobalay.roombooking.application.service.RoomService;
import com.gonzalobalay.roombooking.domain.Room;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AvailableRoomsQueryTests {

    @Autowired
    JpaRoomRepository jpaRoomRepository;
    @Autowired
    RoomService roomService;
    @Autowired
    BookingService bookingService;

    @Test
    void nativeAndJpqlReturnTheSameRooms() {
        Room room = roomService.create("Room 1", 2);
        bookingService.createBookingOnce(room.id(), Instant.parse("2027-01-01T09:00:00Z"), Instant.parse("2027-01-01T10:00:00Z"), "user");
        int minCapacity = 2;
        Instant startsAt = Instant.parse("2027-01-01T10:00:00Z");
        Instant endsAt = Instant.parse("2027-01-01T11:00:00Z");
        List<AvailableRoom> jpql = jpaRoomRepository.findAvailableRooms(minCapacity, startsAt, endsAt);
        List<AvailableRoom> nativeSql = jpaRoomRepository.findAvailableRoomsNative(minCapacity, startsAt, endsAt);
        assertThat(nativeSql).isEqualTo(jpql);
        assertThat(jpql).extracting(AvailableRoom::id).contains(room.id());

        Instant startsAt1 = Instant.parse("2027-01-01T09:30:00Z");
        Instant endsAt1 = Instant.parse("2027-01-01T11:00:00Z");
        List<AvailableRoom> jpql1   = jpaRoomRepository.findAvailableRooms(minCapacity, startsAt1, endsAt1);
        List<AvailableRoom> nativeSql1 = jpaRoomRepository.findAvailableRoomsNative(minCapacity, startsAt1, endsAt1);
        assertThat(nativeSql1).isEqualTo(jpql1);
        assertThat(jpql1).extracting(AvailableRoom::id).doesNotContain(room.id());

        Instant startsAt2 = Instant.parse("2027-01-01T00:00:00Z");
        Instant endsAt2 = Instant.parse("2027-01-01T11:00:00Z");
        List<AvailableRoom> jpql2   = jpaRoomRepository.findAvailableRooms(minCapacity, startsAt2, endsAt2);
        List<AvailableRoom> nativeSql2 = jpaRoomRepository.findAvailableRoomsNative(minCapacity, startsAt2, endsAt2);
        assertThat(nativeSql2).isEqualTo(jpql2);
        assertThat(jpql2).extracting(AvailableRoom::id).doesNotContain(room.id());

    }
}