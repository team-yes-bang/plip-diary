package com.plip.diary.adapter.out.redis;

import com.plip.diary.application.port.out.VideoMetadata;
import com.plip.diary.application.port.out.VideoMetadataCachePort;
import com.plip.diary.global.config.VideoMetadataCacheProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Redis look-aside 캐시 Adapter — 영상 메타(caption·thumbnail) hot cache.
 */
@Component
@RequiredArgsConstructor
public class VideoMetadataRedisAdapter implements VideoMetadataCachePort {

    private final StringRedisTemplate stringRedisTemplate;
    private final VideoMetadataCacheProperties videoMetadataCacheProperties;
    private final VideoMetadataCacheSerde videoMetadataCacheSerde;

    static String cacheKey(UUID userUuid, UUID videoUuid) {
        return VideoMetadataCacheKeys.videoMetaKey(userUuid, videoUuid);
    }

    @Override
    public Map<UUID, VideoMetadata> get(UUID userUuid, List<UUID> videoUuids) {
        if (videoUuids == null || videoUuids.isEmpty()) {
            return Map.of();
        }

        List<String> keys = videoUuids.stream()
                .map(videoUuid -> cacheKey(userUuid, videoUuid))
                .toList();
        List<String> cachedValues = stringRedisTemplate.opsForValue().multiGet(keys);
        if (cachedValues == null) {
            return Map.of();
        }

        Map<UUID, VideoMetadata> result = new HashMap<>();
        for (int index = 0; index < videoUuids.size(); index++) {
            String cachedValue = cachedValues.get(index);
            if (cachedValue == null) {
                continue;
            }
            UUID videoUuid = videoUuids.get(index);
            result.put(videoUuid, videoMetadataCacheSerde.deserialize(videoUuid, cachedValue));
        }
        return result;
    }

    @Override
    public void put(UUID userUuid, Map<UUID, VideoMetadata> metadataByVideoUuid) {
        if (metadataByVideoUuid == null || metadataByVideoUuid.isEmpty()) {
            return;
        }

        Duration ttl = cacheTtl();
        metadataByVideoUuid.forEach((videoUuid, metadata) -> {
            String key = cacheKey(userUuid, videoUuid);
            stringRedisTemplate.opsForValue().set(key, videoMetadataCacheSerde.serialize(metadata), ttl);
        });
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
        return videoMetadataCacheProperties.getCacheTtl();
    }
}
