package com.plip.diary.application.port.out;

import com.plip.diary.domain.model.DiaryVideo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * diary_videos 영속성 포트.
 * <p>조회·count는 Soft Delete row(deleted_at IS NOT NULL)를 제외한다.</p>
 */
public interface DiaryVideoPersistencePort {

    DiaryVideo save(DiaryVideo video);

    Optional<DiaryVideo> findById(Long id);

    Optional<DiaryVideo> findByIdAndUserUuid(Long id, UUID userUuid);

    boolean existsByThemeIdAndVideoUuid(Long themeId, UUID videoUuid);

    long countTodayByUserUuid(UUID userUuid);

    void softDeleteAllByThemeId(Long themeId);

    void softDelete(Long id);

    List<LocalDate> findDistinctWrittenDatesInMonth(UUID userUuid, int year, int month);

    List<DiaryVideo> findByUserUuidAndCreatedAtRange(UUID userUuid, LocalDateTime start, LocalDateTime end);
}
