package com.example.worker.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Map;

@Configuration
public class KafkaConfig {
    @Value("${spring.kafka.bootstrap-servers}") String bootstrap;
    @Value("${app.kafka.topics.received}") String received;
    @Value("${app.kafka.topics.retry}") String retry;
    @Value("${app.kafka.topics.dlq}") String dlq;

    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> props = Map.of(
                org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap,
                org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_ID_CONFIG, "worker",
                org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class,
                JsonDeserializer.TRUSTED_PACKAGES, "*"
        );
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, Object>();
        factory.setConsumerFactory(consumerFactory());
        factory.setConcurrency(3);
        return factory;
    }

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> props = Map.of(
                org.apache.kafka.clients.producer.ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap,
                org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
                org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class
        );
        return new DefaultKafkaProducerFactory<>(props);
    }
    @Bean public KafkaTemplate<String,Object> kafkaTemplate(){ return new KafkaTemplate<>(producerFactory()); }

    @Bean NewTopic topicReceived() { return TopicBuilder.name(received).partitions(3).replicas(1).build(); }
    @Bean NewTopic topicRetry() { return TopicBuilder.name(retry).partitions(3).replicas(1).build(); }
    @Bean NewTopic topicDlq() { return TopicBuilder.name(dlq).partitions(1).replicas(1).build(); }
}