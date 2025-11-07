package com.example.api.repo;

import com.example.api.entity.TransactionEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface TransactionRepository extends ReactiveCrudRepository<TransactionEntity, UUID> {
    Mono<TransactionEntity> findByIdemKey(String idemKey);

    @Query("update transactions set status=:status, updated_at=now() where id=:id returning *")
    Mono<TransactionEntity> updateStatus(UUID id, String status);
}