package com.gonzalobalay.roombooking.infrastructure.out.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

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
    @Version
    private Long version;

    protected RoomJpaEntity() {
    }

    public RoomJpaEntity(UUID id, String name, int capacity, Instant createdAt, Long version) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
        this.createdAt = createdAt;
        this.version = version;
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

    public Long getVersion() {
        return version;
    }
}
