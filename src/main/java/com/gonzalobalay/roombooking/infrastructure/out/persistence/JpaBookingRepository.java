package com.gonzalobalay.roombooking.infrastructure.out.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JpaBookingRepository extends JpaRepository<BookingJpaEntity, UUID> {

//    @Query("select b from BookingJpaEntity b join fetch b.room")
//    List<BookingJpaEntity> findAllWithRoom();

    @EntityGraph(attributePaths = "room")
    @Override
    List<BookingJpaEntity> findAll();

    @EntityGraph(attributePaths = "room")
    @Override
    Page<BookingJpaEntity> findAll(Pageable pageable);
}
