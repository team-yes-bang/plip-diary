package com.plip.diary.adapter.out.persistence;

import com.plip.diary.adapter.out.persistence.entity.DiaryVideoEntity;
import com.plip.diary.adapter.out.persistence.mapper.DiaryPersistenceMapper;
import com.plip.diary.adapter.out.persistence.repository.DiaryThemeJpaRepository;
import com.plip.diary.adapter.out.persistence.repository.DiaryVideoJpaRepository;
import com.plip.diary.application.port.out.DiaryThemePersistencePort;
import com.plip.diary.domain.model.DiaryTheme;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DiaryThemePersistenceAdapter implements DiaryThemePersistencePort {

    private final DiaryThemeJpaRepository diaryThemeJpaRepository;
    private final DiaryVideoJpaRepository diaryVideoJpaRepository;
    private final DiaryPersistenceMapper diaryPersistenceMapper;

    @Override
    public DiaryTheme save(DiaryTheme theme) {
        var entity = diaryPersistenceMapper.toEntity(theme);
        var saved = diaryThemeJpaRepository.save(entity);
        return diaryPersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<DiaryTheme> findById(Long themeId) {
        return diaryThemeJpaRepository.findByThemeIdAndDeletedAtIsNull(themeId)
                .map(diaryPersistenceMapper::toDomain);
    }

    @Override
    public Optional<DiaryTheme> findByIdAndUserUuid(Long themeId, UUID userUuid) {
        return diaryThemeJpaRepository.findByThemeIdAndUserUuidAndDeletedAtIsNull(themeId, userUuid)
                .map(diaryPersistenceMapper::toDomain);
    }

    @Override
    public List<DiaryTheme> findAllByUserUuid(UUID userUuid) {
        return diaryThemeJpaRepository.findByUserUuidAndDeletedAtIsNullOrderByCreatedAtAsc(userUuid)
                .stream()
                .map(diaryPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public long countByUserUuid(UUID userUuid) {
        return diaryThemeJpaRepository.countByUserUuidAndDeletedAtIsNull(userUuid);
    }

    @Override
    public boolean existsByUserUuidAndName(UUID userUuid, String name) {
        return diaryThemeJpaRepository.existsByUserUuidAndNameAndDeletedAtIsNull(userUuid, name);
    }

    @Override
    public boolean existsByUserUuidAndNameExcludingThemeId(UUID userUuid, String name, Long themeId) {
        return diaryThemeJpaRepository.existsByUserUuidAndNameAndThemeIdNotAndDeletedAtIsNull(
                userUuid, name, themeId);
    }

    @Override
    @Transactional
    public void softDeleteWithVideos(Long themeId) {
        var themeEntity = diaryThemeJpaRepository.findByThemeIdAndDeletedAtIsNull(themeId)
                .orElseThrow(() -> new IllegalArgumentException("Theme not found: " + themeId));

        themeEntity.markDeleted();

        diaryVideoJpaRepository.findByTheme_ThemeIdAndDeletedAtIsNull(themeId)
                .forEach(DiaryVideoEntity::markDeleted);
    }
}
