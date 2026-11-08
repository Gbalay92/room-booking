package com.gonzalobalay.roombooking.infrastructure.out.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaBookingRepository extends JpaRepository<BookingJpaEntity, UUID> {

    @EntityGraph(attributePaths = "room")
    @Override
    Page<BookingJpaEntity> findAll(Pageable pageable);
}
