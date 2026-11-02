package com.gonzalobalay.roombooking.infrastructure.in.web.controller;

import com.gonzalobalay.roombooking.application.service.RoomService;
import com.gonzalobalay.roombooking.domain.Room;
import com.gonzalobalay.roombooking.infrastructure.in.web.dto.CreateRoomRequest;
import com.gonzalobalay.roombooking.infrastructure.in.web.dto.RoomResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
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
}
