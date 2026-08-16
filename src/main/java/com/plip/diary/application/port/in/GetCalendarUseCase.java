package com.plip.diary.application.port.in;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface GetCalendarUseCase {

    List<LocalDate> getCalendarDates(UUID userUuid, int year, int month);
}
