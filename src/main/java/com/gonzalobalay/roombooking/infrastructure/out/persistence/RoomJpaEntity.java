package com.gonzalobalay.roombooking.infrastructure.out.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "room")
public class RoomJpaEntity {

    @Id
    private UUID id;

    private String name;
    private int capacity;
    private Instant createdAt;

    protected RoomJpaEntity() {
    }

    public RoomJpaEntity(UUID id, String name, int capacity, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getCapacity() {
        return capacity;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
