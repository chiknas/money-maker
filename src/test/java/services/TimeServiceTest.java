package services;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.Month;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TimeServiceTest {

    @Test
    void getLocalDateTime() {
        // Monday, 1 August 2022 16:54:14.156
        long nanoSeconds = 1659369254156987123L;
        assertEquals(
                LocalDateTime.of(2022, Month.AUGUST, 1, 15, 54, 14, 156987123),
                TimeService.getLocalDateTimeNano(nanoSeconds)
        );
    }

    @Test
    void testGetLocalDateTime() {
        // Monday, 1 August 2022 16:54:14.156
        String nanoSeconds = String.valueOf(1659369254156987123L);
        assertEquals(
                LocalDateTime.of(2022, Month.AUGUST, 1, 15, 54, 14, 156987123),
                TimeService.getLocalDateTimeNano(nanoSeconds)
        );
    }

    @Test
    void getMilliSeconds() {
        LocalDateTime localDateTime = LocalDateTime.of(2022, Month.AUGUST, 1, 15, 54, 14, 156987123);

        assertEquals(
                1659369254156L,
                TimeService.getMilliSeconds(localDateTime)
        );
    }

    @Test
    void getLocalDateTimeSecond() {
        // Monday, 1 August 2022 16:54:14.156
        Double seconds = (double) 1659369254L;
        assertEquals(
                LocalDateTime.of(2022, Month.AUGUST, 1, 15, 54, 14),
                TimeService.getLocalDateTimeSecond(seconds)
        );
    }
}