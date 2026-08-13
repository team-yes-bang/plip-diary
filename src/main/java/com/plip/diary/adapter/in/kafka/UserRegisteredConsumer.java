package com.plip.diary.adapter.in.kafka;

import com.plip.diary.adapter.in.kafka.dto.UserRegisteredEvent;
import com.plip.diary.application.port.in.CreateDefaultThemeUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserRegisteredConsumer {

    private final CreateDefaultThemeUseCase createDefaultThemeUseCase;

    @KafkaListener(
            topics = "${app.kafka.topics.user-registered:user.registered}",
            groupId = "${spring.kafka.consumer.group-id:diary-service}",
            containerFactory = "userRegisteredKafkaListenerContainerFactory"
    )
    public void consume(UserRegisteredEvent event) {
        if (event.userUuid() == null) {
            log.warn("user.registered 이벤트 userUuid 누락 — skip");
            return;
        }
        createDefaultThemeUseCase.createDefaultTheme(event.userUuid());
    }
}
