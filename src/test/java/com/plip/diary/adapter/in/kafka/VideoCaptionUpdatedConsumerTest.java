package com.plip.diary.adapter.in.kafka;

import com.plip.diary.adapter.in.kafka.dto.VideoCaptionUpdatedEvent;
import com.plip.diary.application.service.VideoMetadataSyncService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class VideoCaptionUpdatedConsumerTest {

    @Mock
    private VideoMetadataSyncService videoMetadataSyncService;

    @InjectMocks
    private VideoCaptionUpdatedConsumer videoCaptionUpdatedConsumer;

    @Test
    void consume_patchesWhenValid() {
        UUID userUuid = UUID.randomUUID();
        UUID videoUuid = UUID.randomUUID();
        VideoCaptionUpdatedEvent event = new VideoCaptionUpdatedEvent(videoUuid, userUuid, "수정", null);

        videoCaptionUpdatedConsumer.consume(event);

        verify(videoMetadataSyncService).patchCaptionFromUpdated(userUuid, videoUuid, "수정");
    }

    @Test
    void consume_skipsWhenRequiredFieldsMissing() {
        videoCaptionUpdatedConsumer.consume(new VideoCaptionUpdatedEvent(null, UUID.randomUUID(), "수정", null));
        videoCaptionUpdatedConsumer.consume(new VideoCaptionUpdatedEvent(UUID.randomUUID(), null, "수정", null));

        verify(videoMetadataSyncService, never()).patchCaptionFromUpdated(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any()
        );
    }

    @Test
    void consume_skipsWhenCaptionMissing() {
        UUID userUuid = UUID.randomUUID();
        UUID videoUuid = UUID.randomUUID();

        videoCaptionUpdatedConsumer.consume(new VideoCaptionUpdatedEvent(videoUuid, userUuid, null, null));

        verify(videoMetadataSyncService, never()).patchCaptionFromUpdated(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any()
        );
    }
}
