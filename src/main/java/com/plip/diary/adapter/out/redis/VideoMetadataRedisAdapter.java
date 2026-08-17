package com.plip.diary.adapter.out.redis;

import com.plip.diary.application.port.out.VideoMetadata;
import com.plip.diary.application.port.out.VideoMetadataCachePort;
import com.plip.diary.global.config.QuerySideProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Redis look-aside 캐시 Adapter — CQRS Query side 캐시.
 * <p>Phase 5-2: evict. Phase 5-3: cache-aside 조회·적재.</p>
 */
@Component
@RequiredArgsConstructor
public class VideoMetadataRedisAdapter implements VideoMetadataCachePort {

    private final StringRedisTemplate stringRedisTemplate;
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
        stringRedisTemplate.delete(cacheKey(userUuid, videoUuid));
    }

    @Override
    public void evictAll(UUID userUuid, List<UUID> videoUuids) {
        if (videoUuids == null || videoUuids.isEmpty()) {
            return;
        }
        List<String> keys = videoUuids.stream()
                .map(videoUuid -> cacheKey(userUuid, videoUuid))
                .toList();
        stringRedisTemplate.delete(keys);
    }

    Duration cacheTtl() {
        return querySideProperties.getCacheTtl();
    }
}
