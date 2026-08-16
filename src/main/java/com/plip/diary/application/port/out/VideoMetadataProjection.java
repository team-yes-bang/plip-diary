package com.plip.diary.application.port.out;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * MongoDB 영상 메타 projection(CQRS Query side materialized view) 레코드.
 * <p>Phase 5-2 Kafka 동기화 적재, Phase 5-3 조회 enrichment 소스.</p>
 */
public record VideoMetadataProjection(
        UUID videoUuid,
        UUID userUuid,
        String caption,
        String thumbnailUrl,
        LocalDateTime updatedAt
) {
}
