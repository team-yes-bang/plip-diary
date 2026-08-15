package com.plip.diary.application.port.in;

import com.plip.diary.domain.model.DiaryTheme;

import java.util.UUID;

public interface GetThemeUseCase {

    DiaryTheme getTheme(UUID userUuid, Long id);
}
