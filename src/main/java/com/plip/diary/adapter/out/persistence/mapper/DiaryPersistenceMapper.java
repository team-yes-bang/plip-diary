package com.plip.diary.adapter.out.persistence.mapper;

import com.plip.diary.adapter.out.persistence.entity.DiaryThemeEntity;
import com.plip.diary.adapter.out.persistence.entity.DiaryVideoEntity;
import com.plip.diary.domain.model.DiaryTheme;
import com.plip.diary.domain.model.DiaryVideo;
import org.springframework.stereotype.Component;

@Component
public class DiaryPersistenceMapper {

    public DiaryThemeEntity toEntity(DiaryTheme domain) {
        return DiaryThemeEntity.builder()
                .themeId(domain.getThemeId())
                .userUuid(domain.getUserUuid())
                .name(domain.getName())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .deletedAt(domain.getDeletedAt())
                .build();
    }

    public DiaryTheme toDomain(DiaryThemeEntity entity) {
        return DiaryTheme.reconstitute(
                entity.getThemeId(),
                entity.getUserUuid(),
                entity.getName(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );
    }

    public DiaryVideoEntity toEntity(DiaryVideo domain, DiaryThemeEntity themeEntity) {
        return DiaryVideoEntity.builder()
                .diaryVideoId(domain.getDiaryVideoId())
                .theme(themeEntity)
                .videoUuid(domain.getVideoUuid())
                .createdAt(domain.getCreatedAt())
                .deletedAt(domain.getDeletedAt())
                .build();
    }

    public DiaryVideo toDomain(DiaryVideoEntity entity) {
        return DiaryVideo.reconstitute(
                entity.getDiaryVideoId(),
                entity.getTheme().getThemeId(),
                entity.getVideoUuid(),
                entity.getCreatedAt(),
                entity.getDeletedAt()
        );
    }
}
