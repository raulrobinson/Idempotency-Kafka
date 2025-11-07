package com.example.api.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Map;

@Configuration
public class KafkaConfig {
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrap;

    @Value("${app.kafka.topics.received}")
    private String received;
    @Value("${app.kafka.topics.retry}")
    private String retry;
    @Value("${app.kafka.topics.dlq}")
    private String dlq;

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> props = Map.of(
                org.apache.kafka.clients.producer.ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap,
                org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
                org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class
        );
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() { return new KafkaTemplate<>(producerFactory()); }

    @Bean NewTopic topicReceived() { return TopicBuilder.name(received).partitions(3).replicas(1).build(); }
    @Bean NewTopic topicRetry() { return TopicBuilder.name(retry).partitions(3).replicas(1).build(); }
    @Bean NewTopic topicDlq() { return TopicBuilder.name(dlq).partitions(1).replicas(1).build(); }
}