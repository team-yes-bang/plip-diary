package com.plip.diary.adapter.out.persistence.repository;

import com.plip.diary.adapter.out.persistence.entity.DiaryThemeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DiaryThemeJpaRepository extends JpaRepository<DiaryThemeEntity, Long> {

    boolean existsByUserUuidAndNameAndDeletedAtIsNull(UUID userUuid, String name);

    boolean existsByUserUuidAndNameAndThemeIdNotAndDeletedAtIsNull(UUID userUuid, String name, Long themeId);

    long countByUserUuidAndDeletedAtIsNull(UUID userUuid);

    Optional<DiaryThemeEntity> findByThemeIdAndDeletedAtIsNull(Long themeId);

    Optional<DiaryThemeEntity> findByThemeIdAndUserUuidAndDeletedAtIsNull(Long themeId, UUID userUuid);

    List<DiaryThemeEntity> findByUserUuidAndDeletedAtIsNullOrderByCreatedAtAsc(UUID userUuid);
}
