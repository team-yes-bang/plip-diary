package com.plip.diary.adapter.in.kafka;

import com.plip.diary.adapter.in.kafka.dto.DiaryVideoUploadedEvent;
import com.plip.diary.application.port.in.BindVideoUseCase;
import com.plip.diary.application.service.VideoMetadataSyncService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiaryVideoUploadedConsumerTest {

    @Mock
    private BindVideoUseCase bindVideoUseCase;

    @Mock
    private VideoMetadataSyncService videoMetadataSyncService;

    @InjectMocks
    private DiaryVideoUploadedConsumer diaryVideoUploadedConsumer;

    @Test
    void consume_bindsAndUpsertsProjection() {
        UUID themeUuid = UUID.randomUUID();
        UUID videoUuid = UUID.randomUUID();
        UUID userUuid = UUID.randomUUID();
        DiaryVideoUploadedEvent event = new DiaryVideoUploadedEvent(
                themeUuid,
                videoUuid,
                userUuid,
                "캡션",
                "https://cdn/thumb.jpg"
        );

        when(bindVideoUseCase.bindVideo(themeUuid, videoUuid, userUuid, "캡션", "https://cdn/thumb.jpg"))
                .thenReturn(true);

        diaryVideoUploadedConsumer.consume(event);

        verify(bindVideoUseCase).bindVideo(themeUuid, videoUuid, userUuid, "캡션", "https://cdn/thumb.jpg");
        verify(videoMetadataSyncService).upsertFromBind(userUuid, videoUuid, "캡션", "https://cdn/thumb.jpg");
    }

    @Test
    void consume_upsertsProjectionEvenWhenBindSkipped() {
        UUID themeUuid = UUID.randomUUID();
        UUID videoUuid = UUID.randomUUID();
        UUID userUuid = UUID.randomUUID();
        DiaryVideoUploadedEvent event = new DiaryVideoUploadedEvent(themeUuid, videoUuid, userUuid, null, null);

        when(bindVideoUseCase.bindVideo(themeUuid, videoUuid, userUuid, null, null)).thenReturn(false);

        diaryVideoUploadedConsumer.consume(event);

        verify(videoMetadataSyncService).upsertFromBind(userUuid, videoUuid, null, null);
    }

    @Test
    void consume_skipsWhenRequiredFieldsMissing() {
        diaryVideoUploadedConsumer.consume(new DiaryVideoUploadedEvent(null, UUID.randomUUID(), UUID.randomUUID(), null, null));

        org.mockito.Mockito.verifyNoInteractions(bindVideoUseCase);
        org.mockito.Mockito.verifyNoInteractions(videoMetadataSyncService);
    }
}
