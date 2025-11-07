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

    public ProcessingService(TransactionRepository repo,
                             IdempotencyService idem) {
        this.repo = repo;
        this.idem = idem;
    }

    public Mono<TransactionEntity> process(TransactionEntity t) {
        // Simula latencia realista de un servicio externo (ej: CoreBank)
        return Mono.delay(Duration.ofMillis(400))
                .flatMap(delay -> {
                    // 50% de probabilidad de fallo
                    if (Math.random() < 0.5) {
                        System.out.println("💥 Simulated failure for " + t.idemKey);
                        return Mono.error(new RuntimeException("Simulated transient failure"));
                    }

                    System.out.println("✅ Success for " + t.idemKey);
                    return repo.updateStatus(t.id, "PROCESSED",
                                    t.attempts == null ? 0 : t.attempts)
                            .flatMap(updated -> idem.markDone(t.idemKey).thenReturn(updated));
                });
    }
}
