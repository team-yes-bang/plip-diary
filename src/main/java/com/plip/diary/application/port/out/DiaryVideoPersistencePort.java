package com.plip.diary.application.port.out;

import com.plip.diary.domain.model.DiaryVideo;

import java.util.Optional;

/**
 * diary_videos 영속성 포트.
 * <p>조회·count는 Soft Delete row(deleted_at IS NOT NULL)를 제외한다.</p>
 */
public interface DiaryVideoPersistencePort {

    DiaryVideo save(DiaryVideo video);

    Optional<DiaryVideo> findById(Long id);

    void softDeleteAllByThemeId(Long themeId);
}
