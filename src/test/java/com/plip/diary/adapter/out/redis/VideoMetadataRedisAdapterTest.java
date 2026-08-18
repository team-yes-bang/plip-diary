package com.plip.diary.adapter.out.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.plip.diary.application.port.out.VideoMetadata;
import com.plip.diary.global.config.VideoMetadataCacheProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VideoMetadataRedisAdapterTest {

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private VideoMetadataCacheProperties videoMetadataCacheProperties;

    private VideoMetadataRedisAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new VideoMetadataRedisAdapter(
                stringRedisTemplate,
                videoMetadataCacheProperties,
                new VideoMetadataCacheSerde(new ObjectMapper())
        );
    }

    @Test
    void cacheKey_usesVideoMetadataPrefix() {
        UUID userUuid = UUID.fromString("01912345-6789-7abc-def0-123456789abe");
        UUID videoUuid = UUID.fromString("01912345-6789-7abc-def0-123456789abd");

        assertThat(VideoMetadataRedisAdapter.cacheKey(userUuid, videoUuid))
                .isEqualTo("diary:video-meta:" + userUuid + ":" + videoUuid);
    }

    @Test
    void get_returnsCachedMetadata() {
        UUID userUuid = UUID.fromString("01912345-6789-7abc-def0-123456789abe");
        UUID videoUuid = UUID.fromString("01912345-6789-7abc-def0-123456789abd");
        String key = VideoMetadataRedisAdapter.cacheKey(userUuid, videoUuid);
        String json = "{\"caption\":\"캡션\",\"thumbnailUrl\":\"https://cdn/thumb.jpg\"}";

        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.multiGet(List.of(key))).thenReturn(List.of(json));

        Map<UUID, VideoMetadata> result = adapter.get(userUuid, List.of(videoUuid));

        assertThat(result).containsKey(videoUuid);
        assertThat(result.get(videoUuid).caption()).isEqualTo("캡션");
        assertThat(result.get(videoUuid).thumbnailUrl()).isEqualTo("https://cdn/thumb.jpg");
    }

    @Test
    void get_returnsEmptyWhenNoCacheHits() {
        UUID userUuid = UUID.randomUUID();
        UUID videoUuid = UUID.randomUUID();
        String key = VideoMetadataRedisAdapter.cacheKey(userUuid, videoUuid);

        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.multiGet(List.of(key))).thenReturn(Collections.singletonList(null));

        assertThat(adapter.get(userUuid, List.of(videoUuid))).isEmpty();
    }

    @Test
    void put_storesMetadataWithTtl() {
        UUID userUuid = UUID.randomUUID();
        UUID videoUuid = UUID.randomUUID();
        String key = VideoMetadataRedisAdapter.cacheKey(userUuid, videoUuid);
        VideoMetadata metadata = new VideoMetadata(videoUuid, "캡션", "https://cdn/thumb.jpg");

        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(videoMetadataCacheProperties.getCacheTtl()).thenReturn(Duration.ofHours(12));

        adapter.put(userUuid, Map.of(videoUuid, metadata));

        ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
        verify(valueOperations).set(eq(key), jsonCaptor.capture(), eq(Duration.ofHours(12)));
        assertThat(jsonCaptor.getValue()).contains("캡션").contains("https://cdn/thumb.jpg");
    }

    @Test
    void evict_deletesCacheKey() {
        UUID userUuid = UUID.randomUUID();
        UUID videoUuid = UUID.randomUUID();
        String key = VideoMetadataRedisAdapter.cacheKey(userUuid, videoUuid);

        adapter.evict(userUuid, videoUuid);

        verify(stringRedisTemplate).delete(key);
    }

    @Test
    void evictAll_deletesAllKeys() {
        UUID userUuid = UUID.randomUUID();
        UUID videoUuid1 = UUID.randomUUID();
        UUID videoUuid2 = UUID.randomUUID();
        List<String> keys = List.of(
                VideoMetadataRedisAdapter.cacheKey(userUuid, videoUuid1),
                VideoMetadataRedisAdapter.cacheKey(userUuid, videoUuid2)
        );

        adapter.evictAll(userUuid, List.of(videoUuid1, videoUuid2));

        verify(stringRedisTemplate).delete(keys);
    }

    @Test
    void cacheTtl_usesVideoMetadataCacheProperties() {
        VideoMetadataCacheProperties properties = new VideoMetadataCacheProperties();
        properties.setCacheTtl(Duration.ofMinutes(30));

        VideoMetadataRedisAdapter ttlAdapter = new VideoMetadataRedisAdapter(
                stringRedisTemplate,
                properties,
                new VideoMetadataCacheSerde(new ObjectMapper())
        );

        assertThat(ttlAdapter.cacheTtl()).isEqualTo(Duration.ofMinutes(30));
    }
}
