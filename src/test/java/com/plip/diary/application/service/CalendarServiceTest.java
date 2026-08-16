package com.plip.diary.application.service;

import com.plip.diary.application.port.out.DiaryVideoPersistencePort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CalendarServiceTest {

    @Mock
    private DiaryVideoPersistencePort diaryVideoPersistencePort;

    @InjectMocks
    private CalendarService calendarService;

    @Test
    void getCalendarDates_returnsDatesFromPort() {
        UUID userUuid = UUID.randomUUID();
        List<LocalDate> dates = List.of(LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 15));
        when(diaryVideoPersistencePort.findDistinctWrittenDatesInMonth(userUuid, 2026, 8)).thenReturn(dates);

        List<LocalDate> result = calendarService.getCalendarDates(userUuid, 2026, 8);

        assertThat(result).containsExactlyElementsOf(dates);
        verify(diaryVideoPersistencePort).findDistinctWrittenDatesInMonth(userUuid, 2026, 8);
    }

    @Test
    void getCalendarDates_rejectsInvalidMonth() {
        UUID userUuid = UUID.randomUUID();

        assertThatThrownBy(() -> calendarService.getCalendarDates(userUuid, 2026, 0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> calendarService.getCalendarDates(userUuid, 2026, 13))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void getCalendarDates_rejectsInvalidYear() {
        UUID userUuid = UUID.randomUUID();

        assertThatThrownBy(() -> calendarService.getCalendarDates(userUuid, 1999, 8))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> calendarService.getCalendarDates(userUuid, 2101, 8))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
