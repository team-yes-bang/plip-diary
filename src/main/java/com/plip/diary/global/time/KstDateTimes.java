package com.plip.diary.global.time;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public final class KstDateTimes {

    public static final ZoneId ZONE = ZoneId.of("Asia/Seoul");

    private KstDateTimes() {
    }

    public static LocalDateTime startOfTodayKstAsUtcLocalDateTime() {
        return toUtcLocalDateTime(LocalDate.now(ZONE).atStartOfDay(ZONE));
    }

    public static LocalDateTime startOfTomorrowKstAsUtcLocalDateTime() {
        return toUtcLocalDateTime(LocalDate.now(ZONE).plusDays(1).atStartOfDay(ZONE));
    }

    public static LocalDateTime startOfMonthKstAsUtcLocalDateTime(int year, int month) {
        return toUtcLocalDateTime(LocalDate.of(year, month, 1).atStartOfDay(ZONE));
    }

    public static LocalDateTime startOfNextMonthKstAsUtcLocalDateTime(int year, int month) {
        LocalDate firstOfNextMonth = LocalDate.of(year, month, 1).plusMonths(1);
        return toUtcLocalDateTime(firstOfNextMonth.atStartOfDay(ZONE));
    }

    public static LocalDate toKstLocalDate(LocalDateTime utcLocalDateTime) {
        return utcLocalDateTime.atOffset(ZoneOffset.UTC)
                .atZoneSameInstant(ZONE)
                .toLocalDate();
    }

    private static LocalDateTime toUtcLocalDateTime(ZonedDateTime kstDateTime) {
        return kstDateTime.toInstant().atOffset(ZoneOffset.UTC).toLocalDateTime();
    }
}
