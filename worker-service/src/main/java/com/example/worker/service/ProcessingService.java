package com.example.worker.service;

import com.example.worker.entity.TransactionEntity;
import com.example.worker.repo.TransactionRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
public class ProcessingService {
    private final TransactionRepository repo;
    private final IdempotencyService idem;
    public ProcessingService(TransactionRepository repo, IdempotencyService idem){ this.repo = repo; this.idem = idem; }

    public Mono<TransactionEntity> process(TransactionEntity t){
        // Simulación de lógica de negocio (por ejemplo, invocar terceros, etc.)
        return Mono.delay(Duration.ofMillis(400))
                .then(repo.updateStatus(t.id, "PROCESSED", t.attempts==null?0:t.attempts))
                .flatMap(updated -> idem.markDone(t.idemKey).thenReturn(updated));
    }
}