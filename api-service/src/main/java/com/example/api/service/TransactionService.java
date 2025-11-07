package com.example.api.service;

import com.example.api.dto.TransactionRequest;
import com.example.api.dto.TransactionResponse;
import com.example.api.entity.TransactionEntity;
import com.example.api.repo.TransactionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    public TransactionService(TransactionRepository repo, IdempotencyService idem, KafkaTemplate<String, Object> kafka) {
        this.repo = repo; this.idem = idem; this.kafka = kafka;
    }

    public Mono<TransactionResponse> accept(String idemKey, TransactionRequest req) {
        return idem.tryRegister(idemKey)
                .flatMap(registered -> registered ? createAndPublish(idemKey, req) : findExisting(idemKey));
    }

    private Mono<TransactionResponse> createAndPublish(String idemKey, TransactionRequest req) {
        var e = new TransactionEntity();
        e.id = UUID.randomUUID();
        e.idemKey = idemKey;
        e.amount = req.amount() == null ? BigDecimal.ZERO : req.amount();
        try { e.payload = mapper.writeValueAsString(req.payload()==null?Map.of():req.payload()); }
        catch (JsonProcessingException ex) { return Mono.error(ex); }
        e.status = "RECEIVED"; e.attempts = 0;

        return repo.save(e)
                .doOnSuccess(saved -> kafka.send(topicReceived, saved.idemKey, Map.of(
                        "id", saved.id.toString(),
                        "idemKey", saved.idemKey,
                        "amount", saved.amount,
                        "payload", req.payload()
                )))
                .map(saved -> new TransactionResponse(saved.id.toString(), saved.status, saved.idemKey));
    }

    private Mono<TransactionResponse> findExisting(String idemKey) {
        return repo.findByIdemKey(idemKey)
                .map(t -> new TransactionResponse(t.id.toString(), t.status, t.idemKey));
    }
}