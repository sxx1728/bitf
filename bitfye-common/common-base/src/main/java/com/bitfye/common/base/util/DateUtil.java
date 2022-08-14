package com.bitfye.common.base.util;

import org.apache.commons.lang3.time.DateUtils;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

public class DateUtil {

    private DateUtil() {

    }

    private static Clock clock = Clock.systemDefaultZone();

    public static void useFixedClockAt(Instant instant) {
        clock = Clock.fixed(instant, ZoneId.systemDefault());
    }

    public static void useFixedClockAt(long epochMillis) {
        clock = Clock.fixed(Instant.ofEpochMilli(epochMillis), ZoneId.systemDefault());
    }

    public static void useSystemDefaultZoneClock() {
        clock = Clock.systemDefaultZone();
    }

    public static long currentTimeMillis() {
        return clock.millis();
    }

    public static Long dayStartTime(final Date date) {
        return DateUtils.truncate(date, Calendar.DATE).getTime();
    }

    public static Long dayEndTime(final Date date) {
        return dayStartTime(date) + 86_399_000;
    }

    public static String formatIsoLocalDate(Date date) {
        ZonedDateTime zdt = ZonedDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        return DateTimeFormatter.ISO_LOCAL_DATE.format(zdt);
    }

    public static Date parseIsoLocalDate(String dateString) {
        ZonedDateTime zdt = ZonedDateTime.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE);
        return new Date(zdt.toEpochSecond() * 1000L);
    }

    public static Date parseIsoLocalTime(String timeString) {
        LocalDate ld = LocalDate.now();
        LocalTime lt = LocalTime.parse(timeString, DateTimeFormatter.ISO_LOCAL_TIME);
        ZonedDateTime zdt = ZonedDateTime.of(ld, lt, ZoneId.systemDefault());
        return new Date(zdt.toEpochSecond() * 1000L);
    }

    public static Long localDateTimeToTimestamp(LocalDateTime localDateTime) {
        return localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
}
