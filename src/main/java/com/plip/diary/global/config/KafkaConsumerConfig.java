package com.plip.diary.global.config;

import com.plip.diary.adapter.in.kafka.dto.DiaryVideoUploadedEvent;
import com.plip.diary.adapter.in.kafka.dto.UserRegisteredEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConsumerConfig {

    private static final String KAFKA_DTO_PACKAGE = UserRegisteredEvent.class.getPackageName();

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id:diary-service}")
    private String groupId;

    @Bean
    public ConsumerFactory<String, UserRegisteredEvent> userRegisteredConsumerFactory() {
        return createConsumerFactory(UserRegisteredEvent.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, UserRegisteredEvent> userRegisteredKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, UserRegisteredEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(userRegisteredConsumerFactory());
        return factory;
    }

    @Bean
    public ConsumerFactory<String, DiaryVideoUploadedEvent> diaryVideoUploadedConsumerFactory() {
        return createConsumerFactory(DiaryVideoUploadedEvent.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, DiaryVideoUploadedEvent> diaryVideoUploadedKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, DiaryVideoUploadedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(diaryVideoUploadedConsumerFactory());
        return factory;
    }

    private <T> ConsumerFactory<String, T> createConsumerFactory(Class<T> valueType) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(JsonDeserializer.TRUSTED_PACKAGES, KAFKA_DTO_PACKAGE);
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, valueType.getName());
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        return new DefaultKafkaConsumerFactory<>(props);
    }
}
