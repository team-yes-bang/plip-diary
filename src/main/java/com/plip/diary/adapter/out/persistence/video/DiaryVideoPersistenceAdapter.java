package com.plip.diary.adapter.out.persistence.video;

import com.plip.diary.application.port.out.DiaryThemePersistencePort;
import com.plip.diary.application.port.out.DiaryVideoPersistencePort;
import com.plip.diary.domain.model.DiaryVideo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

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
    @Transactional
    public void softDeleteAllByThemeId(Long themeId) {
        diaryVideoSpringDataRepository.findByThemeIdAndDeletedAtIsNull(themeId)
                .forEach(DiaryVideoJpaEntity::markDeleted);
    }
}
