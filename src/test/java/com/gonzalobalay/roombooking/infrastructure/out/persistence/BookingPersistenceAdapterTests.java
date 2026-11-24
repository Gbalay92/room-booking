package com.gonzalobalay.roombooking.infrastructure.out.persistence;

import com.gonzalobalay.roombooking.TestcontainersConfiguration;
import com.gonzalobalay.roombooking.application.port.BookingSummary;
import com.gonzalobalay.roombooking.application.port.PageResult;
import com.gonzalobalay.roombooking.domain.Booking;
import com.gonzalobalay.roombooking.domain.exception.BookingOverlapException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE)
@Import({TestcontainersConfiguration.class, BookingPersistenceAdapter.class, BookingMapper.class})
@ActiveProfiles("test")
class BookingPersistenceAdapterTests {

    private static final Instant CREATED_AT = Instant.parse("2026-01-01T00:00:00Z");
    private static final Instant TEN = Instant.parse("2027-03-01T10:00:00Z");
    private static final Instant ELEVEN = Instant.parse("2027-03-01T11:00:00Z");
    private static final Instant TWELVE = Instant.parse("2027-03-01T12:00:00Z");
    private static final Instant THIRTEEN = Instant.parse("2027-03-01T13:00:00Z");

    @Autowired
    BookingPersistenceAdapter bookingPersistenceAdapter;

    @Autowired
    JpaRoomRepository jpaRoomRepository;

    @Autowired
    JpaBookingRepository jpaBookingRepository;

    @Test
    void detectsOverlapButNotAdjacentSlots() {
        RoomJpaEntity room = createRoom("Room A");
        createBooking(room, "alice", TEN, ELEVEN);

        assertThat(bookingPersistenceAdapter.existsOverlapping(room.getId(), TEN, ELEVEN)).isTrue();
        assertThat(bookingPersistenceAdapter.existsOverlapping(room.getId(), TEN.plusSeconds(1), ELEVEN)).isTrue();
        assertThat(bookingPersistenceAdapter.existsOverlapping(room.getId(), ELEVEN, TWELVE)).isFalse();
        assertThat(bookingPersistenceAdapter.existsOverlapping(room.getId(), TWELVE, THIRTEEN)).isFalse();
    }

    @Test
    void databaseRejectsOverlapEvenIfTheServiceCheckIsBypassed() {
        RoomJpaEntity room = createRoom("Room B");
        createBooking(room, "bob", TEN, ELEVEN);

        Booking overlapping = new Booking(UUID.randomUUID(), room.getId(), TEN.plusSeconds(30), ELEVEN, "charlie");

        assertThatThrownBy(() -> bookingPersistenceAdapter.save(overlapping))
                .isInstanceOf(BookingOverlapException.class);
    }

    @Test
    void listsOnlyTheBookingsOfTheGivenUser() {
        RoomJpaEntity room = createRoom("Room C");
        createBooking(room, "alice", TEN, ELEVEN);
        createBooking(room, "bob", ELEVEN, TWELVE);
        createBooking(room, "alice", TWELVE, THIRTEEN);

        PageResult<BookingSummary> alice = bookingPersistenceAdapter.findAllByUserName("alice", 0, 10);
        assertThat(alice.totalElements()).isEqualTo(2);
        assertThat(alice.content()).extracting(BookingSummary::startsAt).containsExactly(TWELVE, TEN);

        PageResult<BookingSummary> bob = bookingPersistenceAdapter.findAllByUserName("bob", 0, 10);
        assertThat(bob.totalElements()).isEqualTo(1);
        assertThat(bob.content()).extracting(BookingSummary::userName).containsExactly("bob");

        assertThat(bookingPersistenceAdapter.findAllByUserName("unknown", 0, 10).content()).isEmpty();
    }

    private RoomJpaEntity createRoom(String name) {
        return jpaRoomRepository.saveAndFlush(new RoomJpaEntity(UUID.randomUUID(), name, 4, CREATED_AT, null));
    }

    private void createBooking(RoomJpaEntity room, String userName, Instant startsAt, Instant endsAt) {
        jpaBookingRepository.saveAndFlush(new BookingJpaEntity(UUID.randomUUID(), room, userName, startsAt, endsAt));
    }
}
