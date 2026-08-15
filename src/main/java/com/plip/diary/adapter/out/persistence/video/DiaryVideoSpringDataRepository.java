package com.plip.diary.adapter.out.persistence.video;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface DiaryVideoSpringDataRepository extends JpaRepository<DiaryVideoJpaEntity, Long> {

    Optional<DiaryVideoJpaEntity> findByIdAndDeletedAtIsNull(Long id);

    List<DiaryVideoJpaEntity> findByThemeIdAndDeletedAtIsNull(Long themeId);

    boolean existsByThemeIdAndVideoUuidAndDeletedAtIsNull(Long themeId, UUID videoUuid);

    @Query("""
            SELECT COUNT(v) FROM DiaryVideoJpaEntity v, DiaryThemeJpaEntity t
            WHERE v.themeId = t.id
            AND t.userUuid = :userUuid
            AND v.deletedAt IS NULL
            AND t.deletedAt IS NULL
            AND v.createdAt >= :start
            AND v.createdAt < :end
            """)
    long countTodayByUserUuid(
            @Param("userUuid") UUID userUuid,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}
