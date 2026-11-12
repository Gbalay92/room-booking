package com.gonzalobalay.roombooking.infrastructure.out.persistence;

import com.gonzalobalay.roombooking.application.port.AvailableRoom;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaRoomRepository extends JpaRepository<RoomJpaEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from RoomJpaEntity r where r.id = :id")
    Optional<RoomJpaEntity> findByIdForUpdate(UUID id);

    @Query("""
    select new com.gonzalobalay.roombooking.application.port.AvailableRoom(r.id, r.name, r.capacity)
    from RoomJpaEntity r
    where r.capacity >= :minCapacity
      and not exists (
          select 1 from BookingJpaEntity b
          where b.room.id = r.id
            and b.startsAt < :endsAt
            and b.endsAt > :startsAt
      )
    order by r.capacity, r.name, r.id
    """)
    List<AvailableRoom> findAvailableRooms(int minCapacity, Instant startsAt, Instant endsAt);

    @Query(value = """
    select r.id, r.name, r.capacity
    from room as r
    where r.capacity >= :minCapacity
      and not exists (
          select 1 from booking b
          where b.room_id = r.id
            and tstzrange(b.starts_at, b.ends_at) && tstzrange(:startsAt, :endsAt)
      )
    order by r.capacity, r.name, r.id
    """, nativeQuery = true)
    List<AvailableRoom> findAvailableRoomsNative(int minCapacity, Instant startsAt, Instant endsAt);
}
