package com.plip.diary.application.port.in.dto;

import java.util.List;

public record DateTimelineSection(
        Long themeId,
        String themeName,
        List<DateTimelineVideo> videos
) {
}
