package com.plip.diary.adapter.in.kafka.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserRegisteredEventTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void deserialize_userServicePayload() throws Exception {
        UUID userUuid = UUID.randomUUID();
        String json = """
                {
                  "userUuid": "%s",
                  "email": "user@example.com",
                  "nickname": "플립이",
                  "occurredAt": "2026-08-13T11:00:00"
                }
                """.formatted(userUuid);

        UserRegisteredEvent event = objectMapper.readValue(json, UserRegisteredEvent.class);

        assertThat(event.userUuid()).isEqualTo(userUuid);
    }

    @Test
    void deserialize_snakeCaseUserUuid() throws Exception {
        UUID userUuid = UUID.randomUUID();
        String json = """
                {"user_uuid": "%s"}
                """.formatted(userUuid);

        UserRegisteredEvent event = objectMapper.readValue(json, UserRegisteredEvent.class);

        assertThat(event.userUuid()).isEqualTo(userUuid);
    }
}
