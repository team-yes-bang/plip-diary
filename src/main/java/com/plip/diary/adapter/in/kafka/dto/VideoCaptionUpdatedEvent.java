package com.plip.diary.adapter.in.kafka.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record VideoCaptionUpdatedEvent(
        @JsonProperty("video_uuid")
        @JsonAlias("videoUuid")
        UUID videoUuid,

        @JsonProperty("user_uuid")
        @JsonAlias("userUuid")
        UUID userUuid,

        @JsonProperty("caption")
        String caption,

        @JsonProperty("thumbnail_url")
        @JsonAlias("thumbnailUrl")
        String thumbnailUrl
) {
}
