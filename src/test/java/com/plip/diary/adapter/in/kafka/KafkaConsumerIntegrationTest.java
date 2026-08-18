package com.plip.diary.adapter.in.kafka;

import com.plip.diary.adapter.in.kafka.dto.DiaryVideoUploadedEvent;
import com.plip.diary.adapter.in.kafka.dto.UserRegisteredEvent;
import com.plip.diary.adapter.out.mongodb.VideoMetadataMongoAdapter;
import com.plip.diary.adapter.out.redis.VideoMetadataRedisAdapter;
import com.plip.diary.application.port.out.DiaryThemePersistencePort;
import com.plip.diary.application.port.out.DiaryVideoPersistencePort;
import com.plip.diary.domain.model.DiaryTheme;
import com.plip.diary.domain.model.DiaryVideo;
import com.plip.diary.global.time.KstDateTimes;
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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EmbeddedKafka(
        partitions = 1,
        topics = {"user.registered", "diary.video.uploaded"},
        bootstrapServersProperty = "spring.kafka.bootstrap-servers"
)
@ActiveProfiles({"test", "kafka-test"})
@Import(KafkaConsumerIntegrationTest.KafkaTestProducerConfig.class)
@Execution(ExecutionMode.SAME_THREAD)
class KafkaConsumerIntegrationTest {

    private static final String USER_REGISTERED_TOPIC = "user.registered";
    private static final String DIARY_VIDEO_UPLOADED_TOPIC = "diary.video.uploaded";

    @Autowired
    private KafkaTemplate<String, UserRegisteredEvent> userRegisteredKafkaTemplate;

    @Autowired
    private KafkaTemplate<String, DiaryVideoUploadedEvent> diaryVideoUploadedKafkaTemplate;

    @Autowired
    private KafkaTemplate<String, String> rawKafkaTemplate;

    @Autowired
    private DiaryThemePersistencePort diaryThemePersistencePort;

    @Autowired
    private DiaryVideoPersistencePort diaryVideoPersistencePort;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoBean
    private VideoMetadataMongoAdapter videoMetadataMongoAdapter;

    @MockitoBean
    private VideoMetadataRedisAdapter videoMetadataRedisAdapter;

    @Test
    void consumeUserRegisteredEvent_createsDefaultTheme() throws Exception {
        UUID userUuid = UUID.randomUUID();

        userRegisteredKafkaTemplate.send(USER_REGISTERED_TOPIC, new UserRegisteredEvent(userUuid))
                .get(5, TimeUnit.SECONDS);

        waitUntil(() -> diaryThemePersistencePort.existsByUserUuidAndName(userUuid, "일상"));

        assertThat(diaryThemePersistencePort.existsByUserUuidAndName(userUuid, "일상")).isTrue();
    }

    @Test
    void consumeDiaryVideoUploadedEvent_bindsVideo() throws Exception {
        UUID userUuid = UUID.randomUUID();
        UUID themeUuid = UUID.randomUUID();
        UUID videoUuid = UUID.randomUUID();
        DiaryTheme theme = diaryThemePersistencePort.save(DiaryTheme.create(userUuid, "일상", themeUuid));

        diaryVideoUploadedKafkaTemplate.send(
                DIARY_VIDEO_UPLOADED_TOPIC,
                new DiaryVideoUploadedEvent(themeUuid, videoUuid, userUuid, "캡션", "https://cdn/thumb.jpg")
        ).get(5, TimeUnit.SECONDS);

        waitUntil(() -> diaryVideoPersistencePort.existsByThemeIdAndVideoUuid(theme.getId(), videoUuid));

        assertThat(diaryVideoPersistencePort.existsByThemeIdAndVideoUuid(theme.getId(), videoUuid)).isTrue();
    }

    @Test
    void duplicateDiaryVideoUploadedEvent_keepsSingleVideo() throws Exception {
        UUID userUuid = UUID.randomUUID();
        UUID themeUuid = UUID.randomUUID();
        UUID videoUuid = UUID.randomUUID();
        DiaryTheme theme = diaryThemePersistencePort.save(DiaryTheme.create(userUuid, "일상", themeUuid));
        DiaryVideoUploadedEvent event = new DiaryVideoUploadedEvent(themeUuid, videoUuid, userUuid, null, null);

        diaryVideoUploadedKafkaTemplate.send(DIARY_VIDEO_UPLOADED_TOPIC, event).get(5, TimeUnit.SECONDS);
        diaryVideoUploadedKafkaTemplate.send(DIARY_VIDEO_UPLOADED_TOPIC, event).get(5, TimeUnit.SECONDS);

        waitUntil(() -> diaryVideoPersistencePort.existsByThemeIdAndVideoUuid(theme.getId(), videoUuid));

        seedTodayCreatedAt(theme.getId());

        assertThat(diaryVideoPersistencePort.countTodayByUserUuid(userUuid)).isEqualTo(1);
    }

    @Test
    void dailyVideoLimitExceeded_skipsBinding() throws Exception {
        UUID userUuid = UUID.randomUUID();
        UUID themeUuid = UUID.randomUUID();
        DiaryTheme theme = diaryThemePersistencePort.save(DiaryTheme.create(userUuid, "일상", themeUuid));

        for (int i = 0; i < 20; i++) {
            diaryVideoPersistencePort.save(DiaryVideo.create(theme.getId(), UUID.randomUUID()));
        }
        seedTodayCreatedAt(theme.getId());

        UUID overflowVideoUuid = UUID.randomUUID();
        diaryVideoUploadedKafkaTemplate.send(
                DIARY_VIDEO_UPLOADED_TOPIC,
                new DiaryVideoUploadedEvent(themeUuid, overflowVideoUuid, userUuid, null, null)
        ).get(5, TimeUnit.SECONDS);

        waitUntil(() -> !diaryVideoPersistencePort.existsByThemeIdAndVideoUuid(theme.getId(), overflowVideoUuid));

        assertThat(diaryVideoPersistencePort.countTodayByUserUuid(userUuid)).isEqualTo(20);
        assertThat(diaryVideoPersistencePort.existsByThemeIdAndVideoUuid(theme.getId(), overflowVideoUuid)).isFalse();
    }

    private void seedTodayCreatedAt(Long themeId) {
        jdbcTemplate.update(
                "UPDATE diary_videos SET created_at = ? WHERE theme_id = ?",
                KstDateTimes.startOfToday().plusHours(1),
                themeId
        );
    }

    private void waitUntil(java.util.function.BooleanSupplier condition) throws InterruptedException {
        long deadline = System.nanoTime() + java.time.Duration.ofSeconds(15).toNanos();
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
        ProducerFactory<String, DiaryVideoUploadedEvent> diaryVideoUploadedProducerFactory(
                @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers
        ) {
            return new DefaultKafkaProducerFactory<>(producerProps(bootstrapServers));
        }

        @Bean
        KafkaTemplate<String, DiaryVideoUploadedEvent> diaryVideoUploadedKafkaTemplate(
                ProducerFactory<String, DiaryVideoUploadedEvent> diaryVideoUploadedProducerFactory
        ) {
            return new KafkaTemplate<>(diaryVideoUploadedProducerFactory);
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
