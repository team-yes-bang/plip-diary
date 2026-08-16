package com.plip.diary.application.port.in.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record HomeFeedVideo(
        Long id,
        Long themeId,
        String themeName,
        UUID videoUuid,
        String caption,
        String thumbnailUrl,
        LocalDateTime createdAt
) {
}
