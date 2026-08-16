package com.plip.diary.global.time;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class KstDateTimesTest {

    @Test
    void startOfDay_convertsKstMidnightToUtcNaive() {
        LocalDateTime utc = KstDateTimes.startOfDay(LocalDate.of(2026, 8, 16));

        assertThat(utc).isEqualTo(LocalDateTime.of(2026, 8, 15, 15, 0));
    }

    @Test
    void startOfNextDay_convertsKstMidnightToUtcNaive() {
        LocalDateTime utc = KstDateTimes.startOfNextDay(LocalDate.of(2026, 8, 15));

        assertThat(utc).isEqualTo(LocalDateTime.of(2026, 8, 15, 15, 0));
    }

    @Test
    void toLocalDate_interpretsUtcStoredValueAsKstCalendarDate() {
        LocalDate kstDate = KstDateTimes.toLocalDate(LocalDateTime.of(2026, 8, 15, 16, 0));

        assertThat(kstDate).isEqualTo(LocalDate.of(2026, 8, 16));
    }
}
