package com.plip.diary.adapter.out.mongodb;

import com.plip.diary.application.port.out.VideoMetadataProjection;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class VideoMetadataMongoAdapterTest {

    private final VideoMetadataMongoAdapter adapter = new VideoMetadataMongoAdapter();

    @Test
    void findByUserUuidAndVideoUuids_returnsEmptyStub() {
        UUID userUuid = UUID.randomUUID();
        UUID videoUuid = UUID.randomUUID();

        List<VideoMetadataProjection> result = adapter.findByUserUuidAndVideoUuids(userUuid, List.of(videoUuid));

        assertThat(result).isEmpty();
    }

    @Test
    void findByUserUuidAndVideoUuids_returnsEmptyForBlankInput() {
        assertThat(adapter.findByUserUuidAndVideoUuids(UUID.randomUUID(), List.of())).isEmpty();
        assertThat(adapter.findByUserUuidAndVideoUuids(UUID.randomUUID(), null)).isEmpty();
    }

    @Test
    void upsertAndDelete_areNoOpInPhase51() {
        UUID userUuid = UUID.randomUUID();
        UUID videoUuid = UUID.randomUUID();
        VideoMetadataProjection projection = new VideoMetadataProjection(
                videoUuid,
                userUuid,
                "캡션",
                "https://cdn/thumb.jpg",
                LocalDateTime.of(2026, 8, 16, 7, 30)
        );

        adapter.upsert(projection);
        adapter.deleteByVideoUuid(userUuid, videoUuid);

        assertThat(adapter.findByUserUuidAndVideoUuids(userUuid, List.of(videoUuid))).isEmpty();
    }
}
