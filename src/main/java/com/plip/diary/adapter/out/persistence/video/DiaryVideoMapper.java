package com.plip.diary.adapter.out.persistence.video;

import com.plip.diary.domain.model.DiaryVideo;

class DiaryVideoMapper {

    DiaryVideoJpaEntity toEntity(DiaryVideo domain) {
        return DiaryVideoJpaEntity.builder()
                .id(domain.getId())
                .themeId(domain.getThemeId())
                .videoUuid(domain.getVideoUuid())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .deletedAt(domain.getDeletedAt())
                .build();
    }

    DiaryVideo toDomain(DiaryVideoJpaEntity entity) {
        return DiaryVideo.reconstitute(
                entity.getId(),
                entity.getThemeId(),
                entity.getVideoUuid(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );
    }
}
