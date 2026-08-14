package com.plip.diary.application.port.in;

import com.plip.diary.domain.model.DiaryTheme;

import java.util.UUID;

public interface UpdateThemeUseCase {

    DiaryTheme updateTheme(UUID userUuid, Long themeId, String name);
}
