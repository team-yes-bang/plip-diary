package com.plip.diary.application.port.out;

import java.util.UUID;

/**
 * 영상 메타 projection 쓰기 포트 — Kafka 이벤트 기반 MongoDB upsert·delete.
 * <p>Phase 5-2 구현. Query side projection 유지보수 전용.</p>
 */
public interface VideoMetadataProjectionPort {

    void upsert(VideoMetadataProjection projection);

    void patchCaption(UUID userUuid, UUID videoUuid, String caption);

    void deleteByVideoUuid(UUID userUuid, UUID videoUuid);
}
