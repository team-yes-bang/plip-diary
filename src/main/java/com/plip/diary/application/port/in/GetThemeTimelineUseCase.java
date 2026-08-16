package com.plip.diary.application.port.in;

import com.plip.diary.application.port.in.dto.ThemeTimeline;

import java.util.UUID;

public interface GetThemeTimelineUseCase {

    ThemeTimeline getThemeTimeline(UUID userUuid, Long themeId);
}
