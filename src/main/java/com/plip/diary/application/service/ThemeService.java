package com.plip.diary.application.service;

import com.plip.diary.global.exception.ErrorCode;
import com.plip.diary.global.exception.ThemeLastRemainingException;
import com.plip.diary.global.exception.ThemeLimitExceededException;
import com.plip.diary.global.exception.ThemeNameDuplicateException;
import com.plip.diary.global.exception.ThemeNotFoundException;
import com.plip.diary.application.port.in.CreateThemeUseCase;
import com.plip.diary.application.port.in.DeleteThemeUseCase;
import com.plip.diary.application.port.in.GetThemeUseCase;
import com.plip.diary.application.port.in.ListThemesUseCase;
import com.plip.diary.application.port.in.UpdateThemeUseCase;
import com.plip.diary.application.port.out.DiaryThemePersistencePort;
import com.plip.diary.application.port.out.DiaryVideoPersistencePort;
import com.plip.diary.application.port.out.UuidGeneratorPort;
import com.plip.diary.domain.model.DiaryTheme;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ThemeService implements CreateThemeUseCase, ListThemesUseCase, GetThemeUseCase,
        UpdateThemeUseCase, DeleteThemeUseCase {

    private final DiaryThemePersistencePort diaryThemePersistencePort;
    private final DiaryVideoPersistencePort diaryVideoPersistencePort;
    private final UuidGeneratorPort uuidGeneratorPort;

    @Override
    @Transactional(readOnly = true)
    public List<DiaryTheme> listThemes(UUID userUuid) {
        return diaryThemePersistencePort.findAllByUserUuid(userUuid);
    }

    @Override
    @Transactional(readOnly = true)
    public DiaryTheme getTheme(UUID userUuid, Long id) {
        return diaryThemePersistencePort.findByIdAndUserUuid(id, userUuid)
                .orElseThrow(ThemeNotFoundException::new);
    }

    @Override
    @Transactional
    public DiaryTheme createTheme(UUID userUuid, String name) {
        if (diaryThemePersistencePort.countByUserUuid(userUuid) >= ErrorCode.MAX_ACTIVE_THEMES) {
            throw new ThemeLimitExceededException();
        }
        if (diaryThemePersistencePort.existsByUserUuidAndName(userUuid, name)) {
            throw new ThemeNameDuplicateException();
        }
        return diaryThemePersistencePort.save(DiaryTheme.create(userUuid, name, uuidGeneratorPort.generate()));
    }

    @Override
    @Transactional
    public DiaryTheme updateTheme(UUID userUuid, Long id, String name) {
        DiaryTheme theme = diaryThemePersistencePort.findByIdAndUserUuid(id, userUuid)
                .orElseThrow(ThemeNotFoundException::new);
        if (diaryThemePersistencePort.existsByUserUuidAndNameExcludingId(userUuid, name, id)) {
            throw new ThemeNameDuplicateException();
        }
        return diaryThemePersistencePort.save(theme.rename(name));
    }

    @Override
    @Transactional
    public void deleteTheme(UUID userUuid, Long id) {
        diaryThemePersistencePort.findByIdAndUserUuid(id, userUuid)
                .orElseThrow(ThemeNotFoundException::new);
        if (diaryThemePersistencePort.countByUserUuid(userUuid) <= 1) {
            throw new ThemeLastRemainingException();
        }
        diaryVideoPersistencePort.softDeleteAllByThemeId(id);
        diaryThemePersistencePort.softDelete(id);
    }
}
