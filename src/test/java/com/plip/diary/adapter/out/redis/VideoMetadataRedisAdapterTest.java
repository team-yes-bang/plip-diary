package com.plip.diary.adapter.out.redis;

import com.plip.diary.global.config.QuerySideProperties;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class VideoMetadataRedisAdapterTest {

    private final VideoMetadataRedisAdapter adapter = new VideoMetadataRedisAdapter(new QuerySideProperties());

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
    void cacheTtl_usesQuerySidePropertiesDefault() {
        QuerySideProperties properties = new QuerySideProperties();
        properties.setCacheTtl(Duration.ofMinutes(30));

        VideoMetadataRedisAdapter ttlAdapter = new VideoMetadataRedisAdapter(properties);

        assertThat(ttlAdapter.cacheTtl()).isEqualTo(Duration.ofMinutes(30));
    }
}
