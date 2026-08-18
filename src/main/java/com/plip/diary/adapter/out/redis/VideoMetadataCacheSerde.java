package com.plip.diary.adapter.out.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.plip.diary.application.port.out.VideoMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
class VideoMetadataCacheSerde {

    private final ObjectMapper objectMapper;

    String serialize(VideoMetadata metadata) {
        try {
            return objectMapper.writeValueAsString(new CachePayload(metadata.caption(), metadata.thumbnailUrl()));
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("영상 메타 캐시 직렬화 실패", ex);
        }
    }

    VideoMetadata deserialize(UUID videoUuid, String json) {
        try {
            CachePayload payload = objectMapper.readValue(json, CachePayload.class);
            return new VideoMetadata(videoUuid, payload.caption(), payload.thumbnailUrl());
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("영상 메타 캐시 역직렬화 실패", ex);
        }
    }

    private record CachePayload(String caption, String thumbnailUrl) {
    }
}
