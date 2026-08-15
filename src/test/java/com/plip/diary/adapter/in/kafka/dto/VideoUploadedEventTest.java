package com.plip.diary.adapter.in.kafka.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class VideoUploadedEventTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void deserialize_videoServicePayload() throws Exception {
        UUID themeUuid = UUID.randomUUID();
        UUID videoUuid = UUID.randomUUID();
        UUID userUuid = UUID.randomUUID();
        String json = """
                {
                  "themeUuid": "%s",
                  "videoUuid": "%s",
                  "userUuid": "%s",
                  "occurredAt": "2026-08-13T11:00:00"
                }
                """.formatted(themeUuid, videoUuid, userUuid);

        VideoUploadedEvent event = objectMapper.readValue(json, VideoUploadedEvent.class);

        assertThat(event.themeUuid()).isEqualTo(themeUuid);
        assertThat(event.videoUuid()).isEqualTo(videoUuid);
        assertThat(event.userUuid()).isEqualTo(userUuid);
    }

    @Test
    void deserialize_snakeCaseFields() throws Exception {
        UUID themeUuid = UUID.randomUUID();
        UUID videoUuid = UUID.randomUUID();
        UUID userUuid = UUID.randomUUID();
        String json = """
                {
                  "theme_uuid": "%s",
                  "video_uuid": "%s",
                  "user_uuid": "%s"
                }
                """.formatted(themeUuid, videoUuid, userUuid);

        VideoUploadedEvent event = objectMapper.readValue(json, VideoUploadedEvent.class);

        assertThat(event.themeUuid()).isEqualTo(themeUuid);
        assertThat(event.videoUuid()).isEqualTo(videoUuid);
        assertThat(event.userUuid()).isEqualTo(userUuid);
    }
}
