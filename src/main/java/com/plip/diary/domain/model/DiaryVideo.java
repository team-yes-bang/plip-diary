package com.plip.diary.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class DiaryVideo {

    private final Long diaryVideoId;
    private final Long themeId;
    private final UUID videoUuid;
    private final LocalDateTime createdAt;
    private final LocalDateTime deletedAt;

    private DiaryVideo(
            Long diaryVideoId,
            Long themeId,
            UUID videoUuid,
            LocalDateTime createdAt,
            LocalDateTime deletedAt
    ) {
        this.diaryVideoId = diaryVideoId;
        this.themeId = themeId;
        this.videoUuid = videoUuid;
        this.createdAt = createdAt;
        this.deletedAt = deletedAt;
    }

    public static DiaryVideo create(Long themeId, UUID videoUuid) {
        return new DiaryVideo(null, themeId, videoUuid, null, null);
    }

    public static DiaryVideo reconstitute(
            Long diaryVideoId,
            Long themeId,
            UUID videoUuid,
            LocalDateTime createdAt,
            LocalDateTime deletedAt
    ) {
        return new DiaryVideo(diaryVideoId, themeId, videoUuid, createdAt, deletedAt);
    }

    public boolean isActive() {
        return deletedAt == null;
    }

    public Long getDiaryVideoId() {
        return diaryVideoId;
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

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }
}
