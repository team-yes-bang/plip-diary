package com.plip.diary.adapter.out.persistence.repository;

import com.plip.diary.adapter.out.persistence.entity.DiaryThemeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DiaryThemeJpaRepository extends JpaRepository<DiaryThemeEntity, Long> {

    boolean existsByUserUuidAndNameAndDeletedAtIsNull(UUID userUuid, String name);
}
