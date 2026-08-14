package com.plip.diary.application.port.in;

import java.util.UUID;

public interface DeleteThemeUseCase {

    void deleteTheme(UUID userUuid, Long themeId);
}
