package com.plip.diary.application.port.in.dto;

import java.util.List;

public record ThemeTimelinePage(
        List<ThemeTimelineSection> sections,
        String nextCursor,
        boolean hasMore
) {
}
