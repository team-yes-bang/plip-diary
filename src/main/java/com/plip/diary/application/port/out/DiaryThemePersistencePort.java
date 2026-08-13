package com.plip.diary.application.port.out;

import com.plip.diary.domain.model.DiaryTheme;

import java.util.Optional;

public interface DiaryThemePersistencePort {

    DiaryTheme save(DiaryTheme theme);

    Optional<DiaryTheme> findById(Long themeId);
}
