package com.gonzalobalay.roombooking.infrastructure.out.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "booking")
public class BookingJpaEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id")
    private RoomJpaEntity room;
    private String userName;
    private Instant startsAt;
    private Instant endsAt;


    protected BookingJpaEntity() {
    }

    public BookingJpaEntity(UUID id, RoomJpaEntity room, String userName, Instant startsAt, Instant endsAt) {
        this.id = id;
        this.room = room;
        this.userName = userName;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
    }

    public UUID getId() {
        return id;
    }

    public RoomJpaEntity getRoom() {
        return room;
    }

    public String getUserName() {
        return userName;
    }

    public Instant getStartsAt() {
        return startsAt;
    }

    public Instant getEndsAt() {
        return endsAt;
    }
}
