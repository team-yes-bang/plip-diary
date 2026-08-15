package com.plip.diary.adapter.in.kafka;

import com.plip.diary.adapter.in.kafka.dto.UserRegisteredEvent;
import com.plip.diary.adapter.in.kafka.dto.VideoUploadedEvent;
import com.plip.diary.application.port.out.DiaryThemePersistencePort;
import com.plip.diary.application.port.out.DiaryVideoPersistencePort;
import com.plip.diary.domain.model.DiaryTheme;
import com.plip.diary.domain.model.DiaryVideo;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
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
        topics = {"user.registered", "video.uploaded"},
        bootstrapServersProperty = "spring.kafka.bootstrap-servers"
)
@ActiveProfiles({"test", "kafka-test"})
@Import(KafkaConsumerIntegrationTest.KafkaTestProducerConfig.class)
@Execution(ExecutionMode.SAME_THREAD)
class KafkaConsumerIntegrationTest {

    private static final String USER_REGISTERED_TOPIC = "user.registered";
    private static final String VIDEO_UPLOADED_TOPIC = "video.uploaded";

    @Autowired
    private KafkaTemplate<String, UserRegisteredEvent> userRegisteredKafkaTemplate;

    @Autowired
    private KafkaTemplate<String, VideoUploadedEvent> videoUploadedKafkaTemplate;

    @Autowired
    private KafkaTemplate<String, String> rawKafkaTemplate;

    @Autowired
    private DiaryThemePersistencePort diaryThemePersistencePort;

    @Autowired
    private DiaryVideoPersistencePort diaryVideoPersistencePort;

    @Test
    void consumeUserRegisteredEvent_createsDefaultTheme() throws Exception {
        UUID userUuid = UUID.randomUUID();

        userRegisteredKafkaTemplate.send(USER_REGISTERED_TOPIC, new UserRegisteredEvent(userUuid))
                .get(5, TimeUnit.SECONDS);

        waitUntil(() -> diaryThemePersistencePort.existsByUserUuidAndName(userUuid, "일상"));

        assertThat(diaryThemePersistencePort.existsByUserUuidAndName(userUuid, "일상")).isTrue();
    }

    @Test
    void consumeUserServicePayload_createsDefaultTheme() throws Exception {
        UUID userUuid = UUID.randomUUID();
        String payload = """
                {"userUuid":"%s","email":"user@example.com","nickname":"테스트","occurredAt":"2026-08-13T11:00:00"}
                """.formatted(userUuid);

        rawKafkaTemplate.send(USER_REGISTERED_TOPIC, userUuid.toString(), payload).get(5, TimeUnit.SECONDS);

        waitUntil(() -> diaryThemePersistencePort.existsByUserUuidAndName(userUuid, "일상"));

        assertThat(diaryThemePersistencePort.existsByUserUuidAndName(userUuid, "일상")).isTrue();
    }

    @Test
    void duplicateUserRegisteredEvent_keepsSingleDefaultTheme() throws Exception {
        UUID userUuid = UUID.randomUUID();

        userRegisteredKafkaTemplate.send(USER_REGISTERED_TOPIC, new UserRegisteredEvent(userUuid))
                .get(5, TimeUnit.SECONDS);
        userRegisteredKafkaTemplate.send(USER_REGISTERED_TOPIC, new UserRegisteredEvent(userUuid))
                .get(5, TimeUnit.SECONDS);

        waitUntil(() -> diaryThemePersistencePort.existsByUserUuidAndName(userUuid, "일상"));

        long activeThemeCount = diaryThemePersistencePort.findAllByUserUuid(userUuid).size();

        assertThat(activeThemeCount).isEqualTo(1);
    }

    @Test
    void consumeVideoUploadedEvent_bindsVideo() throws Exception {
        UUID userUuid = UUID.randomUUID();
        UUID themeUuid = UUID.randomUUID();
        UUID videoUuid = UUID.randomUUID();
        DiaryTheme theme = diaryThemePersistencePort.save(DiaryTheme.create(userUuid, "일상", themeUuid));

        videoUploadedKafkaTemplate.send(
                VIDEO_UPLOADED_TOPIC,
                new VideoUploadedEvent(themeUuid, videoUuid, userUuid)
        ).get(5, TimeUnit.SECONDS);

        waitUntil(() -> diaryVideoPersistencePort.existsByThemeIdAndVideoUuid(theme.getId(), videoUuid));

        assertThat(diaryVideoPersistencePort.existsByThemeIdAndVideoUuid(theme.getId(), videoUuid)).isTrue();
    }

    @Test
    void consumeVideoServicePayload_bindsVideo() throws Exception {
        UUID userUuid = UUID.randomUUID();
        UUID themeUuid = UUID.randomUUID();
        UUID videoUuid = UUID.randomUUID();
        DiaryTheme theme = diaryThemePersistencePort.save(DiaryTheme.create(userUuid, "일상", themeUuid));
        String payload = """
                {
                  "themeUuid": "%s",
                  "videoUuid": "%s",
                  "userUuid": "%s",
                  "occurredAt": "2026-08-13T11:00:00"
                }
                """.formatted(themeUuid, videoUuid, userUuid);

        rawKafkaTemplate.send(VIDEO_UPLOADED_TOPIC, videoUuid.toString(), payload).get(5, TimeUnit.SECONDS);

        waitUntil(() -> diaryVideoPersistencePort.existsByThemeIdAndVideoUuid(theme.getId(), videoUuid));

        assertThat(diaryVideoPersistencePort.existsByThemeIdAndVideoUuid(theme.getId(), videoUuid)).isTrue();
    }

    @Test
    void duplicateVideoUploadedEvent_keepsSingleVideo() throws Exception {
        UUID userUuid = UUID.randomUUID();
        UUID themeUuid = UUID.randomUUID();
        UUID videoUuid = UUID.randomUUID();
        DiaryTheme theme = diaryThemePersistencePort.save(DiaryTheme.create(userUuid, "일상", themeUuid));
        VideoUploadedEvent event = new VideoUploadedEvent(themeUuid, videoUuid, userUuid);

        videoUploadedKafkaTemplate.send(VIDEO_UPLOADED_TOPIC, event).get(5, TimeUnit.SECONDS);
        videoUploadedKafkaTemplate.send(VIDEO_UPLOADED_TOPIC, event).get(5, TimeUnit.SECONDS);

        waitUntil(() -> diaryVideoPersistencePort.existsByThemeIdAndVideoUuid(theme.getId(), videoUuid));

        long todayCount = diaryVideoPersistencePort.countTodayByUserUuid(userUuid);
        assertThat(todayCount).isEqualTo(1);
    }

    @Test
    void dailyVideoLimitExceeded_skipsBinding() throws Exception {
        UUID userUuid = UUID.randomUUID();
        UUID themeUuid = UUID.randomUUID();
        DiaryTheme theme = diaryThemePersistencePort.save(DiaryTheme.create(userUuid, "일상", themeUuid));

        for (int i = 0; i < 20; i++) {
            diaryVideoPersistencePort.save(DiaryVideo.create(theme.getId(), UUID.randomUUID()));
        }

        UUID overflowVideoUuid = UUID.randomUUID();
        videoUploadedKafkaTemplate.send(
                VIDEO_UPLOADED_TOPIC,
                new VideoUploadedEvent(themeUuid, overflowVideoUuid, userUuid)
        ).get(5, TimeUnit.SECONDS);

        waitUntil(() -> !diaryVideoPersistencePort.existsByThemeIdAndVideoUuid(theme.getId(), overflowVideoUuid));

        assertThat(diaryVideoPersistencePort.countTodayByUserUuid(userUuid)).isEqualTo(20);
        assertThat(diaryVideoPersistencePort.existsByThemeIdAndVideoUuid(theme.getId(), overflowVideoUuid))
                .isFalse();
    }

    private void waitUntil(java.util.function.BooleanSupplier condition) throws InterruptedException {
        Duration timeout = Duration.ofSeconds(15);
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
            return new DefaultKafkaProducerFactory<>(producerProps(bootstrapServers));
        }

        @Bean
        KafkaTemplate<String, UserRegisteredEvent> userRegisteredKafkaTemplate(
                ProducerFactory<String, UserRegisteredEvent> userRegisteredProducerFactory
        ) {
            return new KafkaTemplate<>(userRegisteredProducerFactory);
        }

        @Bean
        ProducerFactory<String, VideoUploadedEvent> videoUploadedProducerFactory(
                @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers
        ) {
            return new DefaultKafkaProducerFactory<>(producerProps(bootstrapServers));
        }

        @Bean
        KafkaTemplate<String, VideoUploadedEvent> videoUploadedKafkaTemplate(
                ProducerFactory<String, VideoUploadedEvent> videoUploadedProducerFactory
        ) {
            return new KafkaTemplate<>(videoUploadedProducerFactory);
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

        private static Map<String, Object> producerProps(String bootstrapServers) {
            Map<String, Object> props = new HashMap<>();
            props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
            props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
            props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
            return props;
        }
    }
}
