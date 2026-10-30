package com.gonzalobalay.roombooking.application.service;

import com.gonzalobalay.roombooking.application.port.RoomRepository;
import com.gonzalobalay.roombooking.domain.Room;
import com.gonzalobalay.roombooking.domain.exception.RoomNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class RoomService {

    private final RoomRepository roomRepository;

    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    public Room create(String name, int capacity) {
        return roomRepository.save(new Room(UUID.randomUUID(), name, capacity));
    }

    public Room findById(UUID id) {
        return roomRepository.findById(id).orElseThrow(() -> new RoomNotFoundException(id));
    }

    public List<Room> findAll() {
        return roomRepository.findAll();
    }

    public void delete(UUID id) {
        roomRepository.deleteById(id);
    }

}
