package com.gonzalobalay.roombooking.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

@Configuration
public class ClockConfig {

    @Bean
    @Profile("!test")
    public Clock systemClock() {
        return Clock.systemUTC();
    }

    @Bean
    @Profile("test")
    public Clock fixedClock() {
        return Clock.fixed(Instant.parse("2026-01-01T10:00:00Z"), ZoneOffset.UTC);
    }
}
