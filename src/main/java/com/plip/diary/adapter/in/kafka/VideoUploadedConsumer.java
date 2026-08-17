package com.plip.diary.adapter.in.kafka;

import com.plip.diary.adapter.in.kafka.dto.VideoUploadedEvent;
import com.plip.diary.application.port.in.BindVideoUseCase;
import com.plip.diary.application.service.VideoMetadataSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class VideoUploadedConsumer {

    private final BindVideoUseCase bindVideoUseCase;
    private final VideoMetadataSyncService videoMetadataSyncService;

    @KafkaListener(
            topics = "${app.kafka.topics.video-uploaded:video.uploaded}",
            groupId = "${spring.kafka.consumer.group-id:diary-service}",
            containerFactory = "videoUploadedKafkaListenerContainerFactory"
    )
    public void consume(VideoUploadedEvent event) {
        if (event.themeUuid() == null || event.videoUuid() == null || event.userUuid() == null) {
            log.warn(
                    "video.uploaded 이벤트 필수 필드 누락 — skip themeUuid={} videoUuid={} userUuid={}",
                    event.themeUuid(),
                    event.videoUuid(),
                    event.userUuid()
            );
            return;
        }

        bindVideoUseCase.bindVideo(event.themeUuid(), event.videoUuid(), event.userUuid());
        // projection upsert는 MySQL 바인딩 성공 여부와 무관 — 멱등 upsert·Mongo 실패 후 Kafka retry 복구
        videoMetadataSyncService.upsertFromUploaded(
                event.userUuid(),
                event.videoUuid(),
                event.caption(),
                event.thumbnailUrl()
        );
    }
}
