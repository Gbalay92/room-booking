package com.gonzalobalay.roombooking.infrastructure.out.persistence;

import com.gonzalobalay.roombooking.domain.Room;
import org.springframework.stereotype.Component;

@Component
class RoomMapper {

    Room toDomain(RoomJpaEntity entity) {
        return new Room(entity.getId(), entity.getName(), entity.getCapacity(), entity.getCreatedAt());
    }

    RoomJpaEntity toJpaEntity(Room domain) {
        return new RoomJpaEntity(domain.id(), domain.name(), domain.capacity(), domain.createdAt());
    }

}
