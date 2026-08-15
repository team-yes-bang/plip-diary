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

    private static LocalDateTime toUtcLocalDateTime(ZonedDateTime kstDateTime) {
        return kstDateTime.toInstant().atOffset(ZoneOffset.UTC).toLocalDateTime();
    }
}
