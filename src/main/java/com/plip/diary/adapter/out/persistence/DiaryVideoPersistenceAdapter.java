package com.plip.diary.adapter.out.persistence;

import com.plip.diary.adapter.out.persistence.mapper.DiaryPersistenceMapper;
import com.plip.diary.adapter.out.persistence.repository.DiaryThemeJpaRepository;
import com.plip.diary.adapter.out.persistence.repository.DiaryVideoJpaRepository;
import com.plip.diary.application.port.out.DiaryVideoPersistencePort;
import com.plip.diary.domain.model.DiaryVideo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DiaryVideoPersistenceAdapter implements DiaryVideoPersistencePort {

    private final DiaryVideoJpaRepository diaryVideoJpaRepository;
    private final DiaryThemeJpaRepository diaryThemeJpaRepository;
    private final DiaryPersistenceMapper diaryPersistenceMapper;

    @Override
    public DiaryVideo save(DiaryVideo video) {
        var themeEntity = diaryThemeJpaRepository.findById(video.getThemeId())
                .orElseThrow(() -> new IllegalArgumentException("Theme not found: " + video.getThemeId()));
        var entity = diaryPersistenceMapper.toEntity(video, themeEntity);
        var saved = diaryVideoJpaRepository.save(entity);
        return diaryPersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<DiaryVideo> findById(Long diaryVideoId) {
        return diaryVideoJpaRepository.findById(diaryVideoId)
                .map(diaryPersistenceMapper::toDomain);
    }
}
