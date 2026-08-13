package com.plip.diary.adapter.out.persistence;

import com.plip.diary.adapter.out.persistence.mapper.DiaryPersistenceMapper;
import com.plip.diary.adapter.out.persistence.repository.DiaryThemeJpaRepository;
import com.plip.diary.application.port.out.DiaryThemePersistencePort;
import com.plip.diary.domain.model.DiaryTheme;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DiaryThemePersistenceAdapter implements DiaryThemePersistencePort {

    private final DiaryThemeJpaRepository diaryThemeJpaRepository;
    private final DiaryPersistenceMapper diaryPersistenceMapper;

    @Override
    public DiaryTheme save(DiaryTheme theme) {
        var entity = diaryPersistenceMapper.toEntity(theme);
        var saved = diaryThemeJpaRepository.save(entity);
        return diaryPersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<DiaryTheme> findById(Long themeId) {
        return diaryThemeJpaRepository.findById(themeId)
                .map(diaryPersistenceMapper::toDomain);
    }
}
