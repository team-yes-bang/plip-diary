package com.plip.diary.adapter.in.kafka.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UserRegisteredEvent(
        @JsonProperty("user_uuid")
        @JsonAlias("userUuid")
        UUID userUuid
) {
}
