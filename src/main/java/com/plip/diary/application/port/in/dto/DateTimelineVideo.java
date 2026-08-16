package com.plip.diary.application.port.in.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record DateTimelineVideo(
        Long id,
        UUID videoUuid,
        String caption,
        String thumbnailUrl,
        LocalDateTime createdAt
) {
}
