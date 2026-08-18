package com.plip.diary.adapter.in.kafka;

import com.plip.diary.adapter.in.kafka.dto.DiaryVideoUploadedEvent;
import com.plip.diary.application.port.in.BindVideoUseCase;
import com.plip.diary.application.service.VideoMetadataSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DiaryVideoUploadedConsumer {

    private final BindVideoUseCase bindVideoUseCase;
    private final VideoMetadataSyncService videoMetadataSyncService;

    @KafkaListener(
            topics = "${app.kafka.topics.diary-video-uploaded:diary.video.uploaded}",
            groupId = "${spring.kafka.consumer.group-id:diary-service}",
            containerFactory = "diaryVideoUploadedKafkaListenerContainerFactory"
    )
    public void consume(DiaryVideoUploadedEvent event) {
        if (event.themeUuid() == null || event.videoUuid() == null || event.userUuid() == null) {
            log.warn(
                    "diary.video.uploaded 필수 필드 누락 — skip themeUuid={} videoUuid={} userUuid={}",
                    event.themeUuid(),
                    event.videoUuid(),
                    event.userUuid()
            );
            return;
        }

        bindVideoUseCase.bindVideo(
                event.themeUuid(),
                event.videoUuid(),
                event.userUuid(),
                event.caption(),
                event.thumbnailUrl()
        );

        // projection upsert는 MySQL 바인딩 성공 여부와 무관 — 멱등 upsert·Consumer 재처리 복구
        videoMetadataSyncService.upsertFromBind(
                event.userUuid(),
                event.videoUuid(),
                event.caption(),
                event.thumbnailUrl()
        );
    }
}
