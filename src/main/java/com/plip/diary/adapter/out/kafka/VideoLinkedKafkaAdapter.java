package com.plip.diary.adapter.out.kafka;

import com.plip.diary.adapter.out.kafka.dto.VideoLinkedEvent;
import com.plip.diary.application.port.out.VideoLinkedEventPort;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class VideoLinkedKafkaAdapter implements VideoLinkedEventPort {

    private final KafkaTemplate<String, VideoLinkedEvent> videoLinkedKafkaTemplate;

    @Value("${app.kafka.topics.diary-video-linked:diary.video.linked}")
    private String topic;

    @Override
    public void publish(UUID videoUuid) {
        VideoLinkedEvent event = new VideoLinkedEvent(videoUuid, LocalDateTime.now());
        videoLinkedKafkaTemplate.send(topic, videoUuid.toString(), event);
    }
}
