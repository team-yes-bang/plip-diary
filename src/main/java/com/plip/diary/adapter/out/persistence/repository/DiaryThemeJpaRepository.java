package com.plip.diary.adapter.out.persistence.repository;

import com.plip.diary.adapter.out.persistence.entity.DiaryThemeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiaryThemeJpaRepository extends JpaRepository<DiaryThemeEntity, Long> {
}
