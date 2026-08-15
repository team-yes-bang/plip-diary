package com.plip.diary.adapter.out.persistence.theme;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface DiaryThemeSpringDataRepository extends JpaRepository<DiaryThemeJpaEntity, Long> {

    boolean existsByUserUuidAndNameAndDeletedAtIsNull(UUID userUuid, String name);

    boolean existsByUserUuidAndNameAndIdNotAndDeletedAtIsNull(UUID userUuid, String name, Long id);

    long countByUserUuidAndDeletedAtIsNull(UUID userUuid);

    Optional<DiaryThemeJpaEntity> findByIdAndDeletedAtIsNull(Long id);

    Optional<DiaryThemeJpaEntity> findByIdAndUserUuidAndDeletedAtIsNull(Long id, UUID userUuid);

    Optional<DiaryThemeJpaEntity> findByThemeUuidAndDeletedAtIsNull(UUID themeUuid);

    List<DiaryThemeJpaEntity> findByUserUuidAndDeletedAtIsNullOrderByCreatedAtAsc(UUID userUuid);
}
