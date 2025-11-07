package com.example.worker.consumer;

import com.example.worker.entity.TransactionEntity;
import com.example.worker.repo.TransactionRepository;
import com.example.worker.service.ProcessingService;
import com.example.worker.service.IdempotencyService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Component
public class TransactionConsumer {
    private final TransactionRepository repo;
    private final ProcessingService processing;
    private final IdempotencyService idem;
    private final KafkaTemplate<String, Object> kafka;
    private final String retryTopic;

    public TransactionConsumer(TransactionRepository repo, ProcessingService processing, IdempotencyService idem,
                               KafkaTemplate<String, Object> kafka,
                               @Value("${app.kafka.topics.retry}") String retryTopic) {
        this.repo = repo; this.processing = processing; this.idem = idem; this.kafka = kafka; this.retryTopic = retryTopic;
    }

    @KafkaListener(topics = "tx.received", containerFactory = "kafkaListenerContainerFactory")
    public void onMessage(ConsumerRecord<String,Object> record, @Payload Map<String,Object> payload){
        String idemKey = (String) payload.get("idemKey");
        var id = UUID.fromString((String) payload.get("id"));

        repo.findById(id)
                .switchIfEmpty(Mono.defer(() -> {
                    // reconstrucción defensiva si aplica
                    var e = new TransactionEntity();
                    e.id = id; e.idemKey = idemKey; e.amount = new BigDecimal(payload.getOrDefault("amount","0").toString());
                    e.payload = "{}"; e.status = "RECEIVED"; e.attempts = 0; return repo.save(e);
                }))
                .flatMap(t -> idem.tryLockProcessing(idemKey)
                        .flatMap(locked -> locked ? processing.process(t) : Mono.empty())
                )
                .doOnError(ex -> {
                    // Enviar a retry con attempts + 1
                    int attempts = ((Number) payload.getOrDefault("attempts", 0)).intValue() + 1;
                    kafka.send(retryTopic, idemKey, Map.of(
                            "id", payload.get("id"),
                            "idemKey", idemKey,
                            "amount", payload.get("amount"),
                            "payload", payload.get("payload"),
                            "attempts", attempts
                    ));
                })
                .subscribe();
    }
}