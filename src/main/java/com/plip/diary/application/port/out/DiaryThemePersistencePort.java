package com.plip.diary.application.port.out;

import com.plip.diary.domain.model.DiaryTheme;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * diary_themes 영속성 포트.
 * <p>조회·count·exists는 Soft Delete row(deleted_at IS NOT NULL)를 제외한다.</p>
 */
public interface DiaryThemePersistencePort {

    DiaryTheme save(DiaryTheme theme);

    Optional<DiaryTheme> findById(Long themeId);

    Optional<DiaryTheme> findByIdAndUserUuid(Long themeId, UUID userUuid);

    List<DiaryTheme> findAllByUserUuid(UUID userUuid);

    long countByUserUuid(UUID userUuid);

    boolean existsByUserUuidAndName(UUID userUuid, String name);

    boolean existsByUserUuidAndNameExcludingThemeId(UUID userUuid, String name, Long themeId);

    void softDeleteWithVideos(Long themeId);
}
