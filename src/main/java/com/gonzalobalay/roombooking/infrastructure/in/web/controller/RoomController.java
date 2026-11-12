package com.gonzalobalay.roombooking.infrastructure.in.web.controller;

import com.gonzalobalay.roombooking.application.port.AvailableRoom;
import com.gonzalobalay.roombooking.application.service.RoomService;
import com.gonzalobalay.roombooking.domain.Room;
import com.gonzalobalay.roombooking.infrastructure.in.web.dto.CreateRoomRequest;
import com.gonzalobalay.roombooking.infrastructure.in.web.dto.RoomResponse;
import com.gonzalobalay.roombooking.infrastructure.in.web.dto.UpdateRoomRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @PostMapping
    public ResponseEntity<RoomResponse> createRoom(@Valid @RequestBody CreateRoomRequest request) {
        Room room = roomService.create(request.name(), request.capacity());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(room.id())
                .toUri();

        return ResponseEntity.created(location).body(RoomResponse.from(room));
    }

    @GetMapping("/{id}")
    public RoomResponse getRoom(@PathVariable UUID id) {
        Room room = roomService.findById(id);
        return RoomResponse.from(room);
    }

    @GetMapping
    public List<RoomResponse> getRooms() {
        List<Room> rooms = roomService.findAll();
        return rooms.stream().map(RoomResponse::from).toList();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRoom(@PathVariable UUID id) {
        roomService.delete(id);
    }

    @PutMapping("/{id}")
    public RoomResponse updateRoom(@PathVariable UUID id, @Valid @RequestBody UpdateRoomRequest request) {
        Room room = roomService.update(id, request.name(), request.capacity(), request.version());
        return RoomResponse.from(room);
    }

    @GetMapping("/available")
    public List<AvailableRoom> findAvailableRooms(@Min(1) @RequestParam(defaultValue = "1") int minCapacity, @RequestParam Instant startsAt, @RequestParam Instant endsAt) {
        return roomService.findAvailableRooms(minCapacity, startsAt, endsAt);
    }
}
