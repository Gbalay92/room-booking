package com.gonzalobalay.roombooking.application.config;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;


import java.time.Duration;
import java.time.LocalTime;

@ConfigurationProperties(prefix = "booking")
@Validated
public record BookingProperties(
        @NotNull LocalTime openingTime,
        @NotNull LocalTime closingTime,
        @NotNull Duration slotDuration,
        @Positive int maxRoomCapacity) {

    public BookingProperties {
        if (openingTime != null && closingTime != null) {
            if (!closingTime.isAfter(openingTime)) {
                throw new IllegalArgumentException("Opening time must be before closing time");
            }
        }
    }

}