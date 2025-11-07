package com.example.worker.consumer;

import com.example.worker.repo.TransactionRepository;
import com.example.worker.service.ProcessingService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

@Component
public class RetryConsumer {
    private final TransactionRepository repo;
    private final ProcessingService processing;
    private final KafkaTemplate<String,Object> kafka;
    private final int maxAttempts;
    private final long baseBackoffMs;
    private final String dlqTopic;

    public RetryConsumer(TransactionRepository repo, ProcessingService processing, KafkaTemplate<String,Object> kafka,
                         @Value("${app.retry.maxAttempts}") int maxAttempts,
                         @Value("${app.retry.baseBackoffMs}") long baseBackoffMs,
                         @Value("${app.kafka.topics.dlq}") String dlqTopic) {
        this.repo = repo; this.processing = processing; this.kafka = kafka;
        this.maxAttempts = maxAttempts; this.baseBackoffMs = baseBackoffMs; this.dlqTopic = dlqTopic;
    }

    @KafkaListener(
            topics = "tx.retry",
            containerFactory = "kafkaListenerContainerFactory",
            groupId = "worker-retry"
    )
    public void onRetry(ConsumerRecord<String,Object> record, @Payload Map<String,Object> payload) {
        int attempts = ((Number) payload.getOrDefault("attempts", 0)).intValue();
        var id = UUID.fromString((String) payload.get("id"));

        if (attempts > maxAttempts) {
            kafka.send(dlqTopic, (String) payload.get("idemKey"), payload); // DLQ
            return;
        }

        long delay = baseBackoffMs * attempts;
        Mono.delay(java.time.Duration.ofMillis(delay))
                .then(repo.findById(id))
                .flatMap(processing::process)
                .onErrorResume(ex -> {
                    kafka.send("tx.retry", (String) payload.get("idemKey"), Map.of(
                            "id", payload.get("id"),
                            "idemKey", payload.get("idemKey"),
                            "amount", payload.get("amount"),
                            "payload", payload.get("payload"),
                            "attempts", attempts + 1
                    ));
                    return Mono.empty();
                })
                .subscribe();
    }
}