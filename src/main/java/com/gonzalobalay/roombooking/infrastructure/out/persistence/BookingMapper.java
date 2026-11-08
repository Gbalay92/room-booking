package com.gonzalobalay.roombooking.infrastructure.out.persistence;

import com.gonzalobalay.roombooking.application.port.BookingSummary;
import com.gonzalobalay.roombooking.domain.Booking;
import org.springframework.stereotype.Component;

@Component
class BookingMapper {
    private final JpaRoomRepository jpaRoomRepository;

    BookingMapper(JpaRoomRepository jpaRoomRepository) {
        this.jpaRoomRepository = jpaRoomRepository;
    }

    BookingJpaEntity toJpaEntity(Booking booking) {
        return new BookingJpaEntity(booking.id(), jpaRoomRepository.getReferenceById(booking.roomId()), booking.userName(), booking.startsAt(), booking.endsAt());
    }

    Booking toDomainEntity(BookingJpaEntity bookingJpaEntity) {
        return new Booking(bookingJpaEntity.getId(), bookingJpaEntity.getRoom().getId(), bookingJpaEntity.getStartsAt(), bookingJpaEntity.getEndsAt(), bookingJpaEntity.getUserName());
    }

    BookingSummary toSummary(BookingJpaEntity entity) {
        return new BookingSummary(entity.getId(), entity.getRoom().getId(), entity.getRoom().getName(), entity.getStartsAt(), entity.getEndsAt(), entity.getUserName());
    }
}
