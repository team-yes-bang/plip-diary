package com.plip.diary.application.service;

import com.plip.diary.application.port.out.VideoMetadataCachePort;
import com.plip.diary.application.port.out.VideoMetadataProjection;
import com.plip.diary.application.port.out.VideoMetadataProjectionPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class VideoMetadataSyncServiceTest {

    @Mock
    private VideoMetadataProjectionPort videoMetadataProjectionPort;

    @Mock
    private VideoMetadataCachePort videoMetadataCachePort;

    @InjectMocks
    private VideoMetadataSyncService videoMetadataSyncService;

    @Test
    void upsertFromBind_upsertsAndEvicts() {
        UUID userUuid = UUID.randomUUID();
        UUID videoUuid = UUID.randomUUID();

        videoMetadataSyncService.upsertFromBind(userUuid, videoUuid, "캡션", "https://cdn/thumb.jpg");

        ArgumentCaptor<VideoMetadataProjection> captor = ArgumentCaptor.forClass(VideoMetadataProjection.class);
        verify(videoMetadataProjectionPort).upsert(captor.capture());
        VideoMetadataProjection projection = captor.getValue();
        assertThat(projection.videoUuid()).isEqualTo(videoUuid);
        assertThat(projection.userUuid()).isEqualTo(userUuid);
        assertThat(projection.caption()).isEqualTo("캡션");
        assertThat(projection.thumbnailUrl()).isEqualTo("https://cdn/thumb.jpg");
        verify(videoMetadataCachePort).evict(userUuid, videoUuid);
    }

    @Test
    void remove_deletesAndEvicts() {
        UUID userUuid = UUID.randomUUID();
        UUID videoUuid = UUID.randomUUID();

        videoMetadataSyncService.remove(userUuid, videoUuid);

        verify(videoMetadataProjectionPort).deleteByVideoUuid(userUuid, videoUuid);
        verify(videoMetadataCachePort).evict(userUuid, videoUuid);
    }
}
