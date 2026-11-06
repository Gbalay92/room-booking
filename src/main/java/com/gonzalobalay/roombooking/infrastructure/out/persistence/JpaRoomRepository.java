package com.gonzalobalay.roombooking.infrastructure.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaRoomRepository extends JpaRepository<RoomJpaEntity, UUID> {
}
