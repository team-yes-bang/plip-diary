package com.plip.diary.adapter.out.kafka.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.UUID;

public record VideoLinkedEvent(
        @JsonProperty("video_uuid")
        @JsonAlias("videoUuid")
        UUID videoUuid,

        @JsonProperty("occurred_at")
        @JsonAlias("occurredAt")
        LocalDateTime occurredAt
) {
}
