package com.plip.diary.global.config;

import com.plip.diary.adapter.out.kafka.dto.VideoLinkedEvent;
import com.plip.diary.adapter.out.kafka.dto.VideoUnlinkedEvent;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Bean
    public ProducerFactory<String, VideoLinkedEvent> videoLinkedProducerFactory() {
        return new DefaultKafkaProducerFactory<>(producerProps());
    }

    @Bean
    public KafkaTemplate<String, VideoLinkedEvent> videoLinkedKafkaTemplate(
            ProducerFactory<String, VideoLinkedEvent> videoLinkedProducerFactory
    ) {
        return new KafkaTemplate<>(videoLinkedProducerFactory);
    }

    @Bean
    public ProducerFactory<String, VideoUnlinkedEvent> videoUnlinkedProducerFactory() {
        return new DefaultKafkaProducerFactory<>(producerProps());
    }

    @Bean
    public KafkaTemplate<String, VideoUnlinkedEvent> videoUnlinkedKafkaTemplate(
            ProducerFactory<String, VideoUnlinkedEvent> videoUnlinkedProducerFactory
    ) {
        return new KafkaTemplate<>(videoUnlinkedProducerFactory);
    }

    private Map<String, Object> producerProps() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return props;
    }
}
