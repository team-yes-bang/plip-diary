package com.plip.diary.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class DiaryTheme {

    private final Long themeId;
    private final UUID userUuid;
    private final String name;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final LocalDateTime deletedAt;

    private DiaryTheme(
            Long themeId,
            UUID userUuid,
            String name,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            LocalDateTime deletedAt
    ) {
        this.themeId = themeId;
        this.userUuid = userUuid;
        this.name = name;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
    }

    public static DiaryTheme create(UUID userUuid, String name) {
        return new DiaryTheme(null, userUuid, name, null, null, null);
    }

    public static DiaryTheme reconstitute(
            Long themeId,
            UUID userUuid,
            String name,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            LocalDateTime deletedAt
    ) {
        return new DiaryTheme(themeId, userUuid, name, createdAt, updatedAt, deletedAt);
    }

    public boolean isActive() {
        return deletedAt == null;
    }

    public DiaryTheme rename(String name) {
        return new DiaryTheme(themeId, userUuid, name, createdAt, updatedAt, deletedAt);
    }

    public Long getThemeId() {
        return themeId;
    }

    public UUID getUserUuid() {
        return userUuid;
    }

    public String getName() {
        return name;
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
