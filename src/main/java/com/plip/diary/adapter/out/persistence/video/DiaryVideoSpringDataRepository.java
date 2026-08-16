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

    @Query("""
            SELECT v FROM DiaryVideoJpaEntity v, DiaryThemeJpaEntity t
            WHERE v.id = :id
            AND v.themeId = t.id
            AND t.userUuid = :userUuid
            AND v.deletedAt IS NULL
            AND t.deletedAt IS NULL
            """)
    Optional<DiaryVideoJpaEntity> findByIdAndUserUuid(
            @Param("id") Long id,
            @Param("userUuid") UUID userUuid
    );

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

    @Query("""
            SELECT v.createdAt FROM DiaryVideoJpaEntity v, DiaryThemeJpaEntity t
            WHERE v.themeId = t.id
            AND t.userUuid = :userUuid
            AND v.deletedAt IS NULL
            AND t.deletedAt IS NULL
            AND v.createdAt >= :start
            AND v.createdAt < :end
            """)
    List<LocalDateTime> findCreatedAtInMonthByUserUuid(
            @Param("userUuid") UUID userUuid,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
            SELECT v FROM DiaryVideoJpaEntity v, DiaryThemeJpaEntity t
            WHERE v.themeId = t.id
            AND t.userUuid = :userUuid
            AND v.deletedAt IS NULL
            AND t.deletedAt IS NULL
            AND v.createdAt >= :start
            AND v.createdAt < :end
            ORDER BY v.createdAt ASC
            """)
    List<DiaryVideoJpaEntity> findByUserUuidAndCreatedAtRange(
            @Param("userUuid") UUID userUuid,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
            SELECT v FROM DiaryVideoJpaEntity v, DiaryThemeJpaEntity t
            WHERE v.themeId = :themeId
            AND v.themeId = t.id
            AND t.userUuid = :userUuid
            AND v.deletedAt IS NULL
            AND t.deletedAt IS NULL
            ORDER BY v.createdAt DESC
            """)
    List<DiaryVideoJpaEntity> findByThemeIdAndUserUuid(
            @Param("themeId") Long themeId,
            @Param("userUuid") UUID userUuid
    );
}
