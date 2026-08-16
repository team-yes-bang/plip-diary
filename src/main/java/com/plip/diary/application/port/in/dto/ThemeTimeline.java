package com.plip.diary.application.port.in.dto;

import java.util.List;

public record ThemeTimeline(
        List<ThemeTimelineSection> sections
) {
}
