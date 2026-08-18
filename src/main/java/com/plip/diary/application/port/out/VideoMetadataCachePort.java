package com.plip.diary.application.port.out;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 영상 메타 Redis look-aside 캐시 포트 — CQRS Query side.
 */
public interface VideoMetadataCachePort {

    Map<UUID, VideoMetadata> get(UUID userUuid, List<UUID> videoUuids);

    void put(UUID userUuid, Map<UUID, VideoMetadata> metadataByVideoUuid);

    void evict(UUID userUuid, UUID videoUuid);

    void evictAll(UUID userUuid, List<UUID> videoUuids);
}
