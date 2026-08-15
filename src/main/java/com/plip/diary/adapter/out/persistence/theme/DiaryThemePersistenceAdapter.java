package com.plip.diary.adapter.out.persistence.theme;

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

    private final DiaryThemeSpringDataRepository diaryThemeSpringDataRepository;
    private final DiaryThemeMapper diaryThemeMapper = new DiaryThemeMapper();

    @Override
    public DiaryTheme save(DiaryTheme theme) {
        var entity = diaryThemeMapper.toEntity(theme);
        var saved = diaryThemeSpringDataRepository.save(entity);
        return diaryThemeMapper.toDomain(saved);
    }

    @Override
    public Optional<DiaryTheme> findById(Long id) {
        return diaryThemeSpringDataRepository.findByIdAndDeletedAtIsNull(id)
                .map(diaryThemeMapper::toDomain);
    }

    @Override
    public Optional<DiaryTheme> findByIdAndUserUuid(Long id, UUID userUuid) {
        return diaryThemeSpringDataRepository.findByIdAndUserUuidAndDeletedAtIsNull(id, userUuid)
                .map(diaryThemeMapper::toDomain);
    }

    @Override
    public Optional<DiaryTheme> findByThemeUuid(UUID themeUuid) {
        return diaryThemeSpringDataRepository.findByThemeUuidAndDeletedAtIsNull(themeUuid)
                .map(diaryThemeMapper::toDomain);
    }

    @Override
    public List<DiaryTheme> findAllByUserUuid(UUID userUuid) {
        return diaryThemeSpringDataRepository.findByUserUuidAndDeletedAtIsNullOrderByCreatedAtAsc(userUuid)
                .stream()
                .map(diaryThemeMapper::toDomain)
                .toList();
    }

    @Override
    public long countByUserUuid(UUID userUuid) {
        return diaryThemeSpringDataRepository.countByUserUuidAndDeletedAtIsNull(userUuid);
    }

    @Override
    public boolean existsByUserUuidAndName(UUID userUuid, String name) {
        return diaryThemeSpringDataRepository.existsByUserUuidAndNameAndDeletedAtIsNull(userUuid, name);
    }

    @Override
    public boolean existsByUserUuidAndNameExcludingId(UUID userUuid, String name, Long id) {
        return diaryThemeSpringDataRepository.existsByUserUuidAndNameAndIdNotAndDeletedAtIsNull(userUuid, name, id);
    }

    @Override
    @Transactional
    public void softDelete(Long id) {
        var themeEntity = diaryThemeSpringDataRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new IllegalArgumentException("Theme not found: " + id));
        themeEntity.markDeleted();
    }
}
