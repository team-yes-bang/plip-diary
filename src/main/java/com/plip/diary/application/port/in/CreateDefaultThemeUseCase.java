package com.plip.diary.application.port.in;

import java.util.UUID;

public interface CreateDefaultThemeUseCase {

    void createDefaultTheme(UUID userUuid);
}
