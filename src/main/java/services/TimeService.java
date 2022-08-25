package services;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Helper service to handle conversions between different types of time objects.
 * ALL CONVERSIONS TAKE PLACE IN UTC.
 */
public class TimeService {

    public static LocalDateTime getLocalDateTimeNano(String nanoSeconds) {
        return TimeService.getLocalDateTimeNano(Long.parseLong(nanoSeconds));
    }

    public static LocalDateTime getLocalDateTimeNano(long nanoSeconds) {
        long seconds = Long.parseLong(String.valueOf(nanoSeconds)) / 1_000_000_000;
        long nanos = Long.parseLong(String.valueOf(nanoSeconds)) % 1_000_000_000;

        return Instant.ofEpochSecond(seconds, nanos).atZone(ZoneId.of("UTC")).toLocalDateTime();
    }

    public static LocalDateTime getLocalDateTimeSecond(Double seconds) {
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(seconds.longValue()), ZoneId.of("UTC"));
    }

    public static long getMilliSeconds(LocalDateTime dateTime) {
        return dateTime.atZone(ZoneId.of("UTC")).toInstant().toEpochMilli();
    }

    public static LocalDateTime getLocalDateTimeMilliSecond(long milliSeconds) {
        long seconds = milliSeconds / 1000;
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(seconds), ZoneId.of("UTC"));
    }
}
