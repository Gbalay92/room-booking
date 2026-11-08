package com.gonzalobalay.roombooking.infrastructure.out.persistence;

import com.gonzalobalay.roombooking.application.port.BookingRepository;
import com.gonzalobalay.roombooking.application.port.BookingSummary;
import com.gonzalobalay.roombooking.application.port.PageResult;
import com.gonzalobalay.roombooking.domain.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;
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
        BookingJpaEntity saved = jpaBookingRepository.save(bookingMapper.toJpaEntity(booking));
        return bookingMapper.toDomainEntity(saved);
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
    public List<BookingSummary> findAllSummaries() {
        return jpaBookingRepository.findAll().stream().map(bookingMapper::toSummary).toList();
    }

    @Override
    public PageResult<BookingSummary> findAll(int page, int size) {
        Page<BookingJpaEntity> pageResult = jpaBookingRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "startsAt")));
        return new PageResult<>(pageResult.getContent().stream().map(bookingMapper::toSummary).toList(), page, size, pageResult.getTotalElements());
    }
}
