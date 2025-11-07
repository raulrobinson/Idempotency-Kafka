package com.example.api.service;

import com.example.api.dto.TransactionRequest;
import com.example.api.dto.TransactionResponse;
import com.example.api.entity.TransactionEntity;
import com.example.api.repo.TransactionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Service
public class TransactionService {

    private final TransactionRepository repo;
    private final IdempotencyService idem;
    private final KafkaTemplate<String, Object> kafka;
    private final ObjectMapper mapper = new ObjectMapper();
    private final String topicReceived = "tx.received";

    public TransactionService(TransactionRepository repo,
                              IdempotencyService idem,
                              KafkaTemplate<String, Object> kafka) {
        this.repo = repo; this.idem = idem; this.kafka = kafka;
    }

    public Mono<TransactionResponse> accept(String idemKey, TransactionRequest req) {
        System.out.println("accept: " + idemKey + ", req: " + req);
        return idem.tryRegister(idemKey)
                .flatMap(registered -> registered
                        ? createAndPublish(idemKey, req)
                        : findExisting(idemKey)
                )
                .onErrorResume(ex -> ex instanceof DuplicateKeyException,
                        ex -> findExisting(idemKey)); // <- maneja el caso de clave duplicada
    }

    private Mono<TransactionResponse> createAndPublish(String idemKey,
                                                       TransactionRequest req) {
        var e = new TransactionEntity();
        e.setId(UUID.randomUUID());
        e.setIdemKey(idemKey);
        e.setAmount(req.amount() == null ? BigDecimal.ZERO : req.amount());
        e.setPayload(req.payload() == null ? Map.of() : req.payload());
        e.setStatus("RECEIVED");
        e.setAttempts(0);

        System.out.println("e: " + e);
        return repo.save(e)
                .doOnSuccess(saved -> {
                    assert saved.getId() != null;
                    assert req.payload() != null;
                    System.out.println("[1] saved: " + saved);
                    kafka.send(topicReceived, saved.getIdemKey(), Map.of(
                            "id", saved.getId().toString(),
                            "idemKey", saved.getIdemKey(),
                            "amount", saved.getAmount(),
                            "payload", req.payload()
                    ));
                })
                .map(saved -> {
                    assert saved.getId() != null;
                    System.out.println("[2] saved: " + saved);
                    return new TransactionResponse(saved.getId().toString(), saved.getStatus(), saved.getIdemKey());
                });
    }

    private Mono<TransactionResponse> findExisting(String idemKey) {
        System.out.println("findExisting: " + idemKey);
        return repo.findByIdemKey(idemKey)
                .map(t -> {
                    assert t.getId() != null;
                    return new TransactionResponse(t.getId().toString(), t.getStatus(), t.getIdemKey());
                });
    }
}