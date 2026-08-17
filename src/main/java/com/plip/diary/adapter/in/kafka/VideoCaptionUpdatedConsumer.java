package com.plip.diary.adapter.in.kafka;

import com.plip.diary.adapter.in.kafka.dto.VideoCaptionUpdatedEvent;
import com.plip.diary.application.service.VideoMetadataSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class VideoCaptionUpdatedConsumer {

    private final VideoMetadataSyncService videoMetadataSyncService;

    @KafkaListener(
            topics = "${app.kafka.topics.video-caption-updated:video.caption.updated}",
            groupId = "${spring.kafka.consumer.group-id:diary-service}",
            containerFactory = "videoCaptionUpdatedKafkaListenerContainerFactory"
    )
    public void consume(VideoCaptionUpdatedEvent event) {
        if (event.videoUuid() == null || event.userUuid() == null) {
            log.warn(
                    "video.caption.updated 이벤트 필수 필드 누락 — skip videoUuid={} userUuid={}",
                    event.videoUuid(),
                    event.userUuid()
            );
            return;
        }

        if (event.caption() == null) {
            log.warn(
                    "video.caption.updated caption 누락 — skip videoUuid={} userUuid={}",
                    event.videoUuid(),
                    event.userUuid()
            );
            return;
        }

        videoMetadataSyncService.patchCaptionFromUpdated(
                event.userUuid(),
                event.videoUuid(),
                event.caption()
        );
    }
}
