package com.gonzalobalay.roombooking.infrastructure.out.persistence;

import com.gonzalobalay.roombooking.application.port.BookingRepository;
import com.gonzalobalay.roombooking.application.port.BookingSummary;
import com.gonzalobalay.roombooking.application.port.PageResult;
import com.gonzalobalay.roombooking.domain.Booking;
import com.gonzalobalay.roombooking.domain.exception.BookingOverlapException;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public class BookingPersistenceAdapter implements BookingRepository {

    private final BookingMapper bookingMapper;
    private final JpaBookingRepository jpaBookingRepository;

    public BookingPersistenceAdapter(BookingMapper bookingMapper, JpaBookingRepository jpaBookingRepository) {
        this.bookingMapper = bookingMapper;
        this.jpaBookingRepository = jpaBookingRepository;
    }

    @Override
    public Booking save(Booking booking) {
        try {
            BookingJpaEntity saved = jpaBookingRepository.saveAndFlush(bookingMapper.toJpaEntity(booking));
            return bookingMapper.toDomainEntity(saved);
        } catch (DataIntegrityViolationException e) {
            if (e.getCause() instanceof ConstraintViolationException cve
                    && "23P01".equalsIgnoreCase(cve.getSQLState())) { // 23P01 = booking_no_overlap Restriction
                throw new BookingOverlapException(booking.roomId());
            }
            throw e;
        }
    }

    @Override
    public void deleteById(UUID id) {
        jpaBookingRepository.deleteById(id);
    }

    @Override
    public Optional<Booking> findById(UUID id) {
        return jpaBookingRepository.findById(id).map(bookingMapper::toDomainEntity);
    }

    @Override
    public Optional<BookingSummary> findSummaryById(UUID id) {
        return jpaBookingRepository.findById(id).map(bookingMapper::toSummary);
    }

    @Override
    public PageResult<BookingSummary> findAll(int page, int size) {
        Page<BookingJpaEntity> pageResult = jpaBookingRepository.findAll(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "startsAt").and(Sort.by("id"))));
        return new PageResult<>(pageResult.getContent().stream().map(
                bookingMapper::toSummary).toList(), page, size, pageResult.getTotalElements());
    }

    @Override
    public boolean existsOverlapping(UUID roomId, Instant startsAt, Instant endsAt) {
        return jpaBookingRepository.existsOverlapping(roomId, startsAt, endsAt);
    }

}
