package com.plip.diary.adapter.out.kafka;

import com.plip.diary.adapter.out.kafka.dto.VideoDeletedEvent;
import com.plip.diary.application.port.out.VideoDeletedEventPort;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class VideoDeletedKafkaAdapter implements VideoDeletedEventPort {

    private final KafkaTemplate<String, VideoDeletedEvent> videoDeletedKafkaTemplate;

    @Value("${app.kafka.topics.video-deleted:diary.video.deleted}")
    private String topic;

    @Override
    public void publish(UUID videoUuid, UUID userUuid) {
        VideoDeletedEvent event = new VideoDeletedEvent(videoUuid, userUuid, LocalDateTime.now());
        videoDeletedKafkaTemplate.send(topic, videoUuid.toString(), event);
    }
}
