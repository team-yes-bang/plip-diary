package com.plip.diary.application.service;

import com.plip.diary.application.port.in.CreateDefaultThemeUseCase;
import com.plip.diary.application.port.out.DiaryThemePersistencePort;
import com.plip.diary.application.port.out.UuidGeneratorPort;
import com.plip.diary.domain.model.DiaryTheme;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateDefaultThemeService implements CreateDefaultThemeUseCase {

    static final String DEFAULT_THEME_NAME = "일상";

    private final DiaryThemePersistencePort diaryThemePersistencePort;
    private final UuidGeneratorPort uuidGeneratorPort;

    @Override
    @Transactional
    public void createDefaultTheme(UUID userUuid) {
        if (diaryThemePersistencePort.existsByUserUuidAndName(userUuid, DEFAULT_THEME_NAME)) {
            return;
        }
        diaryThemePersistencePort.save(
                DiaryTheme.create(userUuid, DEFAULT_THEME_NAME, uuidGeneratorPort.generate())
        );
    }
}
