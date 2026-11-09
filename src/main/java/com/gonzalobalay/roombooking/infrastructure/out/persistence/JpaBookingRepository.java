package com.gonzalobalay.roombooking.infrastructure.out.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface JpaBookingRepository extends JpaRepository<BookingJpaEntity, UUID> {

    @EntityGraph(attributePaths = "room")
    @Override
    Page<BookingJpaEntity> findAll(Pageable pageable);

    @Query("""
       select count(b) > 0 from BookingJpaEntity b
       where b.room.id = :roomId
         and b.startsAt < :endsAt
         and b.endsAt > :startsAt
       """)
    boolean existsOverlapping(UUID roomId, Instant startsAt, Instant endsAt);

}
