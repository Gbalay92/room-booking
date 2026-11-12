package com.gonzalobalay.roombooking.infrastructure.out.persistence;

import com.gonzalobalay.roombooking.application.port.AvailableRoom;
import com.gonzalobalay.roombooking.application.port.RoomRepository;
import com.gonzalobalay.roombooking.domain.Room;
import com.gonzalobalay.roombooking.domain.exception.RoomConcurrentModificationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class RoomPersistenceAdapter implements RoomRepository {

    private final JpaRoomRepository jpaRoomRepository;
    private final RoomMapper roomMapper;

    public RoomPersistenceAdapter(JpaRoomRepository jpaRoomRepository, RoomMapper roomMapper) {
        this.jpaRoomRepository = jpaRoomRepository;
        this.roomMapper = roomMapper;
    }

    @Override
    public Room save(Room room) {
        try {
            RoomJpaEntity saved = jpaRoomRepository.saveAndFlush(roomMapper.toJpaEntity(room));
            return roomMapper.toDomain(saved);
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new RoomConcurrentModificationException(room.version());
        }
    }

    @Override
    public Optional<Room> findById(UUID id) {
        return jpaRoomRepository.findById(id).map(roomMapper::toDomain);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRoomRepository.deleteById(id);
    }

    @Override
    public List<Room> findAll() {
        return jpaRoomRepository.findAll().stream().map(roomMapper::toDomain).toList();
    }

    @Override
    public Optional<Room> findByIdForUpdate(UUID id) {
        return jpaRoomRepository.findByIdForUpdate(id).map(roomMapper::toDomain);
    }

    @Override
    public List<AvailableRoom> findAvailableRooms(int minCapacity, Instant startsAt, Instant endsAt) {
        return jpaRoomRepository.findAvailableRooms(minCapacity, startsAt, endsAt);
    }

}
