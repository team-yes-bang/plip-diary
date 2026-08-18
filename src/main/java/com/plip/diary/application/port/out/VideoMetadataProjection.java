package com.plip.diary.application.port.out;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * MongoDB 영상 메타 projection(CQRS Query side materialized view) 레코드.
 */
public record VideoMetadataProjection(
        UUID videoUuid,
        UUID userUuid,
        String caption,
        String thumbnailUrl,
        LocalDateTime updatedAt
) {
}
