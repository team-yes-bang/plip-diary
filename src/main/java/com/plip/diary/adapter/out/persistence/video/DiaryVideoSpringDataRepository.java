package com.plip.diary.adapter.out.persistence.video;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

interface DiaryVideoSpringDataRepository extends JpaRepository<DiaryVideoJpaEntity, Long> {

    Optional<DiaryVideoJpaEntity> findByIdAndDeletedAtIsNull(Long id);

    List<DiaryVideoJpaEntity> findByThemeIdAndDeletedAtIsNull(Long themeId);
}
