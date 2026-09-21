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
import com.plip.diary.application.port.out.DiaryTimelineCachePort;
import com.plip.diary.application.port.out.DiaryVideoPersistencePort;
import com.plip.diary.application.port.out.UuidGeneratorPort;
import com.plip.diary.domain.model.DiaryTheme;
import com.plip.diary.domain.model.DiaryVideo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ThemeService implements CreateThemeUseCase, ListThemesUseCase, GetThemeUseCase,
        UpdateThemeUseCase, DeleteThemeUseCase {

    private final DiaryThemePersistencePort diaryThemePersistencePort;
    private final DiaryVideoPersistencePort diaryVideoPersistencePort;
    private final UuidGeneratorPort uuidGeneratorPort;
    private final VideoMetadataSyncService videoMetadataSyncService;
    private final DiaryTimelineCachePort diaryTimelineCachePort;

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
        DiaryTheme updated = diaryThemePersistencePort.save(theme.rename(name));
        scheduleTimelineCacheEvictAfterCommit(userUuid);
        return updated;
    }

    @Override
    @Transactional
    public void deleteTheme(UUID userUuid, Long id) {
        diaryThemePersistencePort.findByIdAndUserUuid(id, userUuid)
                .orElseThrow(ThemeNotFoundException::new);
        if (diaryThemePersistencePort.countByUserUuid(userUuid) <= 1) {
            throw new ThemeLastRemainingException();
        }
        List<UUID> videoUuids = diaryVideoPersistencePort.findByThemeIdAndUserUuid(id, userUuid).stream()
                .map(DiaryVideo::getVideoUuid)
                .toList();
        diaryVideoPersistencePort.softDeleteAllByThemeId(id);
        diaryThemePersistencePort.softDelete(id);
        scheduleAfterCommitCleanup(userUuid, videoUuids);
    }

    private void scheduleAfterCommitCleanup(UUID userUuid, List<UUID> videoUuids) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                if (!videoUuids.isEmpty()) {
                    videoMetadataSyncService.removeAll(userUuid, videoUuids);
                }
                diaryTimelineCachePort.evictByUserUuid(userUuid);
            }
        });
    }

    private void scheduleTimelineCacheEvictAfterCommit(UUID userUuid) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                diaryTimelineCachePort.evictByUserUuid(userUuid);
            }
        });
    }
}
