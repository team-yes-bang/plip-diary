package com.plip.diary.adapter.out.kafka;

import com.plip.diary.adapter.out.kafka.dto.VideoUnlinkedEvent;
import com.plip.diary.application.port.out.VideoUnlinkedEventPort;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class VideoUnlinkedKafkaAdapter implements VideoUnlinkedEventPort {

    private final KafkaTemplate<String, VideoUnlinkedEvent> videoUnlinkedKafkaTemplate;

    @Value("${app.kafka.topics.diary-video-unlinked:diary.video.unlinked}")
    private String topic;

    @Override
    public void publish(UUID videoUuid) {
        VideoUnlinkedEvent event = new VideoUnlinkedEvent(videoUuid, LocalDateTime.now());
        videoUnlinkedKafkaTemplate.send(topic, videoUuid.toString(), event);
    }
}
