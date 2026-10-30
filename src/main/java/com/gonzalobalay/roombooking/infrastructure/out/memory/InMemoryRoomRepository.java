package com.gonzalobalay.roombooking.infrastructure.out.memory;

import com.gonzalobalay.roombooking.application.port.RoomRepository;
import com.gonzalobalay.roombooking.domain.Room;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryRoomRepository implements RoomRepository {

    private final Map<UUID, Room> rooms = new ConcurrentHashMap<>();

    @Override
    public Room save(Room room) {
        rooms.put(room.id(), room);
        return room;
    }

    @Override
    public Optional<Room> findById(UUID id) {
        return Optional.ofNullable(rooms.get(id));
    }

    @Override
    public void deleteById(UUID id) {
        rooms.remove(id);
    }

    @Override
    public List<Room> findAll() {
        return List.copyOf(rooms.values());
    }
}
