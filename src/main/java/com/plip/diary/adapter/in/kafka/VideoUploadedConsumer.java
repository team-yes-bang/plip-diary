package com.plip.diary.adapter.in.kafka;

import com.plip.diary.adapter.in.kafka.dto.VideoUploadedEvent;
import com.plip.diary.application.port.in.BindVideoUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class VideoUploadedConsumer {

    private final BindVideoUseCase bindVideoUseCase;

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
    }
}
