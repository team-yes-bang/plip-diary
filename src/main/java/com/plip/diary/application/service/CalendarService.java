package com.plip.diary.application.service;

import com.plip.diary.application.port.in.GetCalendarUseCase;
import com.plip.diary.application.port.out.DiaryVideoPersistencePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CalendarService implements GetCalendarUseCase {

    private static final int MIN_YEAR = 2000;
    private static final int MAX_YEAR = 2100;

    private final DiaryVideoPersistencePort diaryVideoPersistencePort;

    @Override
    public List<LocalDate> getCalendarDates(UUID userUuid, int year, int month) {
        validateYearMonth(year, month);
        return diaryVideoPersistencePort.findDistinctWrittenDatesInMonth(userUuid, year, month);
    }

    private void validateYearMonth(int year, int month) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("month must be between 1 and 12");
        }
        if (year < MIN_YEAR || year > MAX_YEAR) {
            throw new IllegalArgumentException("year must be between " + MIN_YEAR + " and " + MAX_YEAR);
        }
    }
}
