package com.plip.diary.application.port.in;

import com.plip.diary.application.port.in.dto.ThemeTimelinePage;

import java.util.UUID;

public interface GetThemeTimelineUseCase {

    ThemeTimelinePage getThemeTimeline(UUID userUuid, Long themeId, String cursor, int limit);
}
