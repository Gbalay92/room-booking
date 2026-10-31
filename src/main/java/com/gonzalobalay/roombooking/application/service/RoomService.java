package com.gonzalobalay.roombooking.application.service;

import com.gonzalobalay.roombooking.application.config.BookingProperties;
import com.gonzalobalay.roombooking.application.port.RoomRepository;
import com.gonzalobalay.roombooking.domain.Room;
import com.gonzalobalay.roombooking.domain.exception.RoomNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final Clock clock;
    private final BookingProperties bookingProperties;

    public RoomService(RoomRepository roomRepository, Clock clock, BookingProperties bookingProperties) {
        this.roomRepository = roomRepository;
        this.clock = clock;
        this.bookingProperties = bookingProperties;
    }

    public Room create(String name, int capacity) {
        if (capacity > bookingProperties.maxRoomCapacity()) {
            throw new IllegalArgumentException("Room capacity cannot exceed " + bookingProperties.maxRoomCapacity());
        }
        return roomRepository.save(new Room(UUID.randomUUID(), name, capacity, Instant.now(clock)));
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
