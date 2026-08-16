package com.plip.diary.application.port.in.dto;

import java.time.LocalDate;
import java.util.List;

public record ThemeTimelineSection(
        LocalDate date,
        List<ThemeTimelineVideo> videos
) {
}
