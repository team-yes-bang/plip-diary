package com.plip.diary.adapter.out.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.plip.diary.application.port.out.VideoMetadata;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class VideoMetadataCacheSerdeTest {

    private final VideoMetadataCacheSerde serde = new VideoMetadataCacheSerde(new ObjectMapper());

    @Test
    void roundTrip_preservesCaptionAndThumbnailUrl() {
        UUID videoUuid = UUID.fromString("01912345-6789-7abc-def0-123456789abd");
        VideoMetadata metadata = new VideoMetadata(videoUuid, "캡션", "https://cdn/thumb.jpg");

        String json = serde.serialize(metadata);
        VideoMetadata restored = serde.deserialize(videoUuid, json);

        assertThat(restored).isEqualTo(metadata);
    }
}
