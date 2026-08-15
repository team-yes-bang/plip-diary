package com.plip.diary.adapter.out.persistence.video;

import com.plip.diary.application.port.out.DiaryThemePersistencePort;
import com.plip.diary.application.port.out.DiaryVideoPersistencePort;
import com.plip.diary.domain.model.DiaryVideo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.plip.diary.global.time.KstDateTimes;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DiaryVideoPersistenceAdapter implements DiaryVideoPersistencePort {

    private final DiaryVideoSpringDataRepository diaryVideoSpringDataRepository;
    private final DiaryThemePersistencePort diaryThemePersistencePort;
    private final DiaryVideoMapper diaryVideoMapper = new DiaryVideoMapper();

    @Override
    public DiaryVideo save(DiaryVideo video) {
        diaryThemePersistencePort.findById(video.getThemeId())
                .orElseThrow(() -> new IllegalArgumentException("Theme not found: " + video.getThemeId()));
        var entity = diaryVideoMapper.toEntity(video);
        var saved = diaryVideoSpringDataRepository.save(entity);
        return diaryVideoMapper.toDomain(saved);
    }

    @Override
    public Optional<DiaryVideo> findById(Long id) {
        return diaryVideoSpringDataRepository.findByIdAndDeletedAtIsNull(id)
                .map(diaryVideoMapper::toDomain);
    }

    @Override
    public boolean existsByThemeIdAndVideoUuid(Long themeId, UUID videoUuid) {
        return diaryVideoSpringDataRepository.existsByThemeIdAndVideoUuidAndDeletedAtIsNull(themeId, videoUuid);
    }

    @Override
    public long countTodayByUserUuid(UUID userUuid) {
        LocalDateTime start = KstDateTimes.startOfTodayKstAsUtcLocalDateTime();
        LocalDateTime end = KstDateTimes.startOfTomorrowKstAsUtcLocalDateTime();
        return diaryVideoSpringDataRepository.countTodayByUserUuid(userUuid, start, end);
    }

    @Override
    @Transactional
    public void softDeleteAllByThemeId(Long themeId) {
        diaryVideoSpringDataRepository.findByThemeIdAndDeletedAtIsNull(themeId)
                .forEach(DiaryVideoJpaEntity::markDeleted);
    }
}
