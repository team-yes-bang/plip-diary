package com.plip.diary.adapter.in.kafka;

import com.plip.diary.adapter.in.kafka.dto.VideoUploadedEvent;
import com.plip.diary.application.port.in.BindVideoUseCase;
import com.plip.diary.application.service.VideoMetadataSyncService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VideoUploadedConsumerTest {

    @Mock
    private BindVideoUseCase bindVideoUseCase;

    @Mock
    private VideoMetadataSyncService videoMetadataSyncService;

    @InjectMocks
    private VideoUploadedConsumer videoUploadedConsumer;

    @Test
    void consume_upsertsProjectionWhenBound() {
        UUID themeUuid = UUID.randomUUID();
        UUID videoUuid = UUID.randomUUID();
        UUID userUuid = UUID.randomUUID();
        VideoUploadedEvent event = new VideoUploadedEvent(
                themeUuid, videoUuid, userUuid, "캡션", "https://cdn/thumb.jpg"
        );

        when(bindVideoUseCase.bindVideo(themeUuid, videoUuid, userUuid)).thenReturn(true);

        videoUploadedConsumer.consume(event);

        verify(videoMetadataSyncService).upsertFromUploaded(userUuid, videoUuid, "캡션", "https://cdn/thumb.jpg");
    }

    @Test
    void consume_upsertsProjectionEvenWhenBindSkipped() {
        UUID themeUuid = UUID.randomUUID();
        UUID videoUuid = UUID.randomUUID();
        UUID userUuid = UUID.randomUUID();
        VideoUploadedEvent event = new VideoUploadedEvent(themeUuid, videoUuid, userUuid, null, null);

        when(bindVideoUseCase.bindVideo(themeUuid, videoUuid, userUuid)).thenReturn(false);

        videoUploadedConsumer.consume(event);

        verify(videoMetadataSyncService).upsertFromUploaded(userUuid, videoUuid, null, null);
    }

    @Test
    void consume_skipsWhenRequiredFieldsMissing() {
        videoUploadedConsumer.consume(new VideoUploadedEvent(null, UUID.randomUUID(), UUID.randomUUID(), null, null));

        verify(bindVideoUseCase, never()).bindVideo(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any()
        );
    }
}
