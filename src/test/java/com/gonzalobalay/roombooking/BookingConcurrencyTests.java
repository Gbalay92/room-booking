package com.gonzalobalay.roombooking;

import com.gonzalobalay.roombooking.application.service.BookingService;
import com.gonzalobalay.roombooking.application.service.RoomService;
import com.gonzalobalay.roombooking.domain.exception.BookingOverlapException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class BookingConcurrencyTests {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private RoomService roomService;

    @Test
    void onlyOneOfConcurrentOverlappingBookingsSucceeds() throws Exception {
        UUID roomId = roomService.create("Concurrency room", 5).id();
        Instant start = Instant.parse("2030-01-01T10:00:00Z");
        Instant end = start.plus(1, ChronoUnit.HOURS);
        int threads = 10;

        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch go = new CountDownLatch(1);
        AtomicInteger successes = new AtomicInteger();
        AtomicInteger conflicts = new AtomicInteger();
        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < threads; i++) {
            futures.add(pool.submit(() -> {
                ready.countDown();
                go.await();
                try {
                    bookingService.createBooking(roomId, start, end, "user");
                    successes.incrementAndGet();
                } catch (BookingOverlapException e) {
                    conflicts.incrementAndGet();
                }
                return null;
            }));
        }
        ready.await();
        go.countDown();
        pool.shutdown();
        pool.awaitTermination(10, TimeUnit.SECONDS);

        for (Future<?> future : futures) {
            future.get();
        }

        assertThat(successes.get() + conflicts.get()).isEqualTo(threads);
        assertThat(successes.get()).isEqualTo(1);
    }

}
