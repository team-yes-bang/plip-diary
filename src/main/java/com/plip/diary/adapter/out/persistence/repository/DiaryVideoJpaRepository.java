package com.plip.diary.adapter.out.persistence.repository;

import com.plip.diary.adapter.out.persistence.entity.DiaryVideoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DiaryVideoJpaRepository extends JpaRepository<DiaryVideoEntity, Long> {

    Optional<DiaryVideoEntity> findByDiaryVideoIdAndDeletedAtIsNull(Long diaryVideoId);

    List<DiaryVideoEntity> findByTheme_ThemeIdAndDeletedAtIsNull(Long themeId);
}
