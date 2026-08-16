package com.plip.diary.global.time;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 * KST 달력(일·월 경계) 판단용 유틸 — 조회·집계 비즈니스 로직 전용.
 * <p>DB {@code DATETIME}은 배포·CI JVM(UTC) 기준 {@link LocalDateTime#now()} wall-clock(UTC naive)으로 저장된다.
 * 경계 메서드는 KST 달력 경계를 UTC {@link LocalDateTime}으로 변환해 {@code created_at} 비교에 사용한다.</p>
 * <p>저장 경로(JPA Entity 등)에서는 {@link LocalDateTime#now()}만 사용하고 본 클래스를 쓰지 않는다.</p>
 */
public final class KstDateTimes {

    public static final ZoneId ZONE = ZoneId.of("Asia/Seoul");

    private KstDateTimes() {
    }

    public static LocalDate today() {
        return LocalDate.now(ZONE);
    }

    /** KST 기준 해당 일 00:00 → DB 비교용 UTC naive LocalDateTime. */
    public static LocalDateTime startOfDay(LocalDate date) {
        return toUtcLocalDateTime(date.atStartOfDay(ZONE));
    }

    /** KST 기준 다음 일 00:00 → DB 비교용 UTC naive LocalDateTime. */
    public static LocalDateTime startOfNextDay(LocalDate date) {
        return toUtcLocalDateTime(date.plusDays(1).atStartOfDay(ZONE));
    }

    public static LocalDateTime startOfToday() {
        return startOfDay(today());
    }

    public static LocalDateTime startOfTomorrow() {
        return startOfNextDay(today());
    }

    public static LocalDateTime startOfMonth(int year, int month) {
        return startOfDay(LocalDate.of(year, month, 1));
    }

    public static LocalDateTime startOfNextMonth(int year, int month) {
        return startOfDay(LocalDate.of(year, month, 1).plusMonths(1));
    }

    /** DB UTC naive LocalDateTime → KST 달력 날짜. */
    public static LocalDate toLocalDate(LocalDateTime utcStoredDateTime) {
        return utcStoredDateTime.atOffset(ZoneOffset.UTC).atZoneSameInstant(ZONE).toLocalDate();
    }

    private static LocalDateTime toUtcLocalDateTime(ZonedDateTime kstDateTime) {
        return kstDateTime.toInstant().atOffset(ZoneOffset.UTC).toLocalDateTime();
    }
}
