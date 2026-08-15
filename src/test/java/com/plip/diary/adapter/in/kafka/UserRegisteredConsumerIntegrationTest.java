package com.plip.diary.adapter.in.kafka;

import com.plip.diary.adapter.in.kafka.dto.UserRegisteredEvent;
import com.plip.diary.application.port.out.DiaryThemePersistencePort;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EmbeddedKafka(
        partitions = 1,
        topics = {"user.registered"},
        bootstrapServersProperty = "spring.kafka.bootstrap-servers"
)
@ActiveProfiles({"test", "kafka-test"})
@Import(UserRegisteredConsumerIntegrationTest.KafkaTestProducerConfig.class)
class UserRegisteredConsumerIntegrationTest {

    private static final String TOPIC = "user.registered";

    @Autowired
    private KafkaTemplate<String, UserRegisteredEvent> kafkaTemplate;

    @Autowired
    private KafkaTemplate<String, String> rawKafkaTemplate;

    @Autowired
    private DiaryThemePersistencePort diaryThemePersistencePort;

    @Test
    void consumeUserRegisteredEvent_createsDefaultTheme() throws Exception {
        UUID userUuid = UUID.randomUUID();

        kafkaTemplate.send(TOPIC, new UserRegisteredEvent(userUuid)).get(5, TimeUnit.SECONDS);

        waitUntil(() -> diaryThemePersistencePort.existsByUserUuidAndName(userUuid, "일상"));

        assertThat(diaryThemePersistencePort.existsByUserUuidAndName(userUuid, "일상")).isTrue();
    }

    @Test
    void consumeUserServicePayload_createsDefaultTheme() throws Exception {
        UUID userUuid = UUID.randomUUID();
        String payload = """
                {"userUuid":"%s","email":"user@example.com","nickname":"테스트","occurredAt":"2026-08-13T11:00:00"}
                """.formatted(userUuid);

        rawKafkaTemplate.send(TOPIC, userUuid.toString(), payload).get(5, TimeUnit.SECONDS);

        waitUntil(() -> diaryThemePersistencePort.existsByUserUuidAndName(userUuid, "일상"));

        assertThat(diaryThemePersistencePort.existsByUserUuidAndName(userUuid, "일상")).isTrue();
    }

    @Test
    void duplicateEvent_keepsSingleDefaultTheme() throws Exception {
        UUID userUuid = UUID.randomUUID();

        kafkaTemplate.send(TOPIC, new UserRegisteredEvent(userUuid)).get(5, TimeUnit.SECONDS);
        kafkaTemplate.send(TOPIC, new UserRegisteredEvent(userUuid)).get(5, TimeUnit.SECONDS);

        waitUntil(() -> diaryThemePersistencePort.existsByUserUuidAndName(userUuid, "일상"));

        long activeThemeCount = diaryThemePersistencePort.findAllByUserUuid(userUuid).size();

        assertThat(activeThemeCount).isEqualTo(1);
    }

    private void waitUntil(java.util.function.BooleanSupplier condition) throws InterruptedException {
        Duration timeout = Duration.ofSeconds(10);
        long deadline = System.nanoTime() + timeout.toNanos();
        while (System.nanoTime() < deadline) {
            if (condition.getAsBoolean()) {
                return;
            }
            Thread.sleep(200);
        }
        assertThat(condition.getAsBoolean()).isTrue();
    }

    @TestConfiguration
    static class KafkaTestProducerConfig {

        @Bean
        ProducerFactory<String, UserRegisteredEvent> userRegisteredProducerFactory(
                @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers
        ) {
            Map<String, Object> props = new HashMap<>();
            props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
            props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
            props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
            return new DefaultKafkaProducerFactory<>(props);
        }

        @Bean
        KafkaTemplate<String, UserRegisteredEvent> userRegisteredKafkaTemplate(
                ProducerFactory<String, UserRegisteredEvent> userRegisteredProducerFactory
        ) {
            return new KafkaTemplate<>(userRegisteredProducerFactory);
        }

        @Bean
        ProducerFactory<String, String> rawProducerFactory(
                @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers
        ) {
            Map<String, Object> props = new HashMap<>();
            props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
            props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
            props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
            return new DefaultKafkaProducerFactory<>(props);
        }

        @Bean
        KafkaTemplate<String, String> rawKafkaTemplate(ProducerFactory<String, String> rawProducerFactory) {
            return new KafkaTemplate<>(rawProducerFactory);
        }
    }
}
