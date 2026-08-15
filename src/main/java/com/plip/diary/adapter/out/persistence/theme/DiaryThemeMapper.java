package com.plip.diary.adapter.out.persistence.theme;

import com.plip.diary.domain.model.DiaryTheme;

class DiaryThemeMapper {

    DiaryThemeJpaEntity toEntity(DiaryTheme domain) {
        return DiaryThemeJpaEntity.builder()
                .id(domain.getId())
                .themeUuid(domain.getThemeUuid())
                .userUuid(domain.getUserUuid())
                .name(domain.getName())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .deletedAt(domain.getDeletedAt())
                .build();
    }

    DiaryTheme toDomain(DiaryThemeJpaEntity entity) {
        return DiaryTheme.reconstitute(
                entity.getId(),
                entity.getThemeUuid(),
                entity.getUserUuid(),
                entity.getName(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );
    }
}
