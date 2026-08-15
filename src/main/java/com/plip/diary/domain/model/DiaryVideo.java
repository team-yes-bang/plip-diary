package com.plip.diary.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class DiaryVideo {

    private final Long id;
    private final Long themeId;
    private final UUID videoUuid;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final LocalDateTime deletedAt;

    private DiaryVideo(
            Long id,
            Long themeId,
            UUID videoUuid,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            LocalDateTime deletedAt
    ) {
        this.id = id;
        this.themeId = themeId;
        this.videoUuid = videoUuid;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
    }

    public static DiaryVideo create(Long themeId, UUID videoUuid) {
        return new DiaryVideo(null, themeId, videoUuid, null, null, null);
    }

    public static DiaryVideo reconstitute(
            Long id,
            Long themeId,
            UUID videoUuid,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            LocalDateTime deletedAt
    ) {
        return new DiaryVideo(id, themeId, videoUuid, createdAt, updatedAt, deletedAt);
    }

    public boolean isActive() {
        return deletedAt == null;
    }

    public Long getId() {
        return id;
    }

    public Long getThemeId() {
        return themeId;
    }

    public UUID getVideoUuid() {
        return videoUuid;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }
}
