package com.plip.diary.adapter.out.query;

import com.plip.diary.application.port.out.VideoMetadata;
import com.plip.diary.application.port.out.VideoMetadataCachePort;
import com.plip.diary.application.port.out.VideoMetadataProjection;
import com.plip.diary.application.port.out.VideoMetadataQueryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VideoMetadataQueryAdapterTest {

    @Mock
    private VideoMetadataCachePort videoMetadataCachePort;

    @Mock
    private VideoMetadataQueryPort videoMetadataQueryPort;

    private VideoMetadataQueryAdapter videoMetadataQueryAdapter;

    private UUID userUuid;
    private UUID videoUuid1;
    private UUID videoUuid2;

    @BeforeEach
    void setUp() {
        videoMetadataQueryAdapter = new VideoMetadataQueryAdapter(
                videoMetadataCachePort,
                videoMetadataQueryPort
        );
        userUuid = UUID.fromString("01912345-6789-7abc-def0-123456789abe");
        videoUuid1 = UUID.fromString("01912345-6789-7abc-def0-123456789abd");
        videoUuid2 = UUID.fromString("01912345-6789-7abc-def0-123456789abf");
    }

    @Test
    void fetchVideoMetadata_returnsCacheHitsWithoutMongo() {
        VideoMetadata cached = new VideoMetadata(videoUuid1, "캐시", "https://cdn/cache.jpg");
        when(videoMetadataCachePort.get(userUuid, List.of(videoUuid1))).thenReturn(Map.of(videoUuid1, cached));

        Map<UUID, VideoMetadata> result = videoMetadataQueryAdapter.fetchVideoMetadata(userUuid, List.of(videoUuid1));

        assertThat(result).containsEntry(videoUuid1, cached);
        verify(videoMetadataQueryPort, never()).findByUserUuidAndVideoUuids(eq(userUuid), eq(List.of(videoUuid1)));
    }

    @Test
    void fetchVideoMetadata_loadsMongoMissesAndCachesThem() {
        VideoMetadata mongoMetadata = new VideoMetadata(videoUuid1, "몽고", "https://cdn/mongo.jpg");
        when(videoMetadataCachePort.get(userUuid, List.of(videoUuid1))).thenReturn(Map.of());
        when(videoMetadataQueryPort.findByUserUuidAndVideoUuids(userUuid, List.of(videoUuid1)))
                .thenReturn(List.of(new VideoMetadataProjection(
                        videoUuid1,
                        userUuid,
                        mongoMetadata.caption(),
                        mongoMetadata.thumbnailUrl(),
                        LocalDateTime.of(2026, 8, 16, 7, 30)
                )));

        Map<UUID, VideoMetadata> result = videoMetadataQueryAdapter.fetchVideoMetadata(userUuid, List.of(videoUuid1));

        assertThat(result).containsEntry(videoUuid1, mongoMetadata);
        verify(videoMetadataCachePort).put(userUuid, Map.of(videoUuid1, mongoMetadata));
    }

    @Test
    void fetchVideoMetadata_returnsPartialResultWhenProjectionMissing() {
        when(videoMetadataCachePort.get(userUuid, List.of(videoUuid1))).thenReturn(Map.of());
        when(videoMetadataQueryPort.findByUserUuidAndVideoUuids(userUuid, List.of(videoUuid1)))
                .thenReturn(List.of());

        Map<UUID, VideoMetadata> result = videoMetadataQueryAdapter.fetchVideoMetadata(userUuid, List.of(videoUuid1));

        assertThat(result).isEmpty();
        verify(videoMetadataCachePort, never()).put(eq(userUuid), eq(Map.of()));
    }

    @Test
    void fetchVideoMetadata_returnsEmptyForEmptyInput() {
        assertThat(videoMetadataQueryAdapter.fetchVideoMetadata(userUuid, List.of())).isEmpty();
    }

    @Test
    void fetchVideoMetadata_mergesCacheAndMongoLayers() {
        VideoMetadata cached = new VideoMetadata(videoUuid1, "캐시", "https://cdn/cache.jpg");
        VideoMetadata mongoMetadata = new VideoMetadata(videoUuid2, "몽고", "https://cdn/mongo.jpg");

        when(videoMetadataCachePort.get(userUuid, List.of(videoUuid1, videoUuid2)))
                .thenReturn(Map.of(videoUuid1, cached));
        when(videoMetadataQueryPort.findByUserUuidAndVideoUuids(userUuid, List.of(videoUuid2)))
                .thenReturn(List.of(new VideoMetadataProjection(
                        videoUuid2,
                        userUuid,
                        mongoMetadata.caption(),
                        mongoMetadata.thumbnailUrl(),
                        LocalDateTime.of(2026, 8, 16, 7, 30)
                )));

        Map<UUID, VideoMetadata> result = videoMetadataQueryAdapter.fetchVideoMetadata(
                userUuid,
                List.of(videoUuid1, videoUuid2)
        );

        assertThat(result)
                .containsEntry(videoUuid1, cached)
                .containsEntry(videoUuid2, mongoMetadata);
        verify(videoMetadataCachePort).put(userUuid, Map.of(videoUuid2, mongoMetadata));
    }
}
