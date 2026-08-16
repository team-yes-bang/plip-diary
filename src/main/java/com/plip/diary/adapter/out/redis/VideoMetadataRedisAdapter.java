package com.plip.diary.adapter.out.redis;

import com.plip.diary.application.port.out.VideoMetadata;
import com.plip.diary.application.port.out.VideoMetadataCachePort;
import com.plip.diary.global.config.QuerySideProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Redis look-aside 캐시 Adapter — CQRS Query side 캐시.
 * <p>Phase 5-1: 키 규칙·TTL 상수만 확정, 조회·적재는 stub. Phase 5-3에서 구현.</p>
 */
@Component
@RequiredArgsConstructor
public class VideoMetadataRedisAdapter implements VideoMetadataCachePort {

    private final QuerySideProperties querySideProperties;

    static String cacheKey(UUID userUuid, UUID videoUuid) {
        return VideoMetadataCacheKeys.videoMetaKey(userUuid, videoUuid);
    }

    @Override
    public Map<UUID, VideoMetadata> get(UUID userUuid, List<UUID> videoUuids) {
        return Map.of();
    }

    @Override
    public void put(UUID userUuid, Map<UUID, VideoMetadata> metadataByVideoUuid) {
        // Phase 5-3에서 구현
    }

    @Override
    public void evict(UUID userUuid, UUID videoUuid) {
        // Phase 5-2에서 구현
    }

    @Override
    public void evictAll(UUID userUuid, List<UUID> videoUuids) {
        // Phase 5-2에서 구현
    }

    Duration cacheTtl() {
        return querySideProperties.getCacheTtl();
    }
}
