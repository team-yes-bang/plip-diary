package com.plip.diary.application.port.out;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Redis look-aside 캐시 포트 — CQRS Query side.
 * <p>Phase 5-1: 인터페이스·stub Adapter. Phase 5-2 evict, Phase 5-3 cache-aside 조회.</p>
 */
public interface VideoMetadataCachePort {

    Map<UUID, VideoMetadata> get(UUID userUuid, List<UUID> videoUuids);

    void put(UUID userUuid, Map<UUID, VideoMetadata> metadataByVideoUuid);

    void evict(UUID userUuid, UUID videoUuid);

    void evictAll(UUID userUuid, List<UUID> videoUuids);
}
