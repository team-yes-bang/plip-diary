package com.plip.diary.adapter.out.redis;

import com.plip.diary.global.config.QuerySideProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class VideoMetadataRedisAdapterTest {

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private QuerySideProperties querySideProperties;

    @InjectMocks
    private VideoMetadataRedisAdapter adapter;

    @Test
    void cacheKey_usesVideoMetadataPrefix() {
        UUID userUuid = UUID.fromString("01912345-6789-7abc-def0-123456789abe");
        UUID videoUuid = UUID.fromString("01912345-6789-7abc-def0-123456789abd");

        assertThat(VideoMetadataRedisAdapter.cacheKey(userUuid, videoUuid))
                .isEqualTo("diary:video-meta:" + userUuid + ":" + videoUuid);
    }

    @Test
    void get_returnsEmptyStub() {
        assertThat(adapter.get(UUID.randomUUID(), List.of(UUID.randomUUID()))).isEmpty();
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
    void cacheTtl_usesQuerySidePropertiesDefault() {
        QuerySideProperties properties = new QuerySideProperties();
        properties.setCacheTtl(Duration.ofMinutes(30));

        VideoMetadataRedisAdapter ttlAdapter = new VideoMetadataRedisAdapter(stringRedisTemplate, properties);

        assertThat(ttlAdapter.cacheTtl()).isEqualTo(Duration.ofMinutes(30));
    }
}
