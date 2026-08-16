package com.plip.diary.application.port.out;

import java.util.List;
import java.util.UUID;

/**
 * 영상 메타 projection 조회 포트 — CQRS Query side read.
 * <p>Phase 5-3에서 {@link VideoServicePort} enrichment 구현체가 사용.</p>
 */
public interface VideoMetadataQueryPort {

    List<VideoMetadataProjection> findByUserUuidAndVideoUuids(UUID userUuid, List<UUID> videoUuids);
}
