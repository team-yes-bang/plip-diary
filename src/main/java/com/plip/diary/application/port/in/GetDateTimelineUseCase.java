package com.plip.diary.application.port.in;

import com.plip.diary.application.port.in.dto.DateTimeline;
import com.plip.diary.application.port.in.dto.DateWindowTimeline;

import java.time.LocalDate;
import java.util.UUID;

public interface GetDateTimelineUseCase {

    DateTimeline getDateTimeline(UUID userUuid, LocalDate date);

    DateWindowTimeline getDateWindowTimeline(UUID userUuid, LocalDate focusDate, int window);
}
