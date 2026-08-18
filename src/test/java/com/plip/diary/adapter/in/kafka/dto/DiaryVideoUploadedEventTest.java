package com.plip.diary.adapter.in.kafka.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DiaryVideoUploadedEventTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void deserializesCamelCasePayload() throws Exception {
        UUID themeUuid = UUID.randomUUID();
        UUID videoUuid = UUID.randomUUID();
        UUID userUuid = UUID.randomUUID();
        String json = """
                {
                  "themeUuid": "%s",
                  "videoUuid": "%s",
                  "userUuid": "%s",
                  "caption": "캡션",
                  "thumbnailUrl": "https://cdn/thumb.jpg"
                }
                """.formatted(themeUuid, videoUuid, userUuid);

        DiaryVideoUploadedEvent event = objectMapper.readValue(json, DiaryVideoUploadedEvent.class);

        assertThat(event.themeUuid()).isEqualTo(themeUuid);
        assertThat(event.videoUuid()).isEqualTo(videoUuid);
        assertThat(event.userUuid()).isEqualTo(userUuid);
        assertThat(event.caption()).isEqualTo("캡션");
        assertThat(event.thumbnailUrl()).isEqualTo("https://cdn/thumb.jpg");
    }

    @Test
    void deserializesSnakeCasePayload() throws Exception {
        UUID themeUuid = UUID.randomUUID();
        UUID videoUuid = UUID.randomUUID();
        UUID userUuid = UUID.randomUUID();
        String json = """
                {
                  "theme_uuid": "%s",
                  "video_uuid": "%s",
                  "user_uuid": "%s",
                  "thumbnail_url": "https://cdn/thumb.jpg"
                }
                """.formatted(themeUuid, videoUuid, userUuid);

        DiaryVideoUploadedEvent event = objectMapper.readValue(json, DiaryVideoUploadedEvent.class);

        assertThat(event.themeUuid()).isEqualTo(themeUuid);
        assertThat(event.videoUuid()).isEqualTo(videoUuid);
        assertThat(event.userUuid()).isEqualTo(userUuid);
        assertThat(event.thumbnailUrl()).isEqualTo("https://cdn/thumb.jpg");
    }
}
