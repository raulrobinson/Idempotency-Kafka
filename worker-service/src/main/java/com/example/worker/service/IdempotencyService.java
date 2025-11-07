package com.example.worker.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
public class IdempotencyService {
    private final ReactiveStringRedisTemplate redis;
    private final Duration ttl;
    public IdempotencyService(ReactiveStringRedisTemplate redis,
                              @Value("${app.idempotency.ttlSeconds:3600}") long ttlSeconds) {
        this.redis = redis; this.ttl = Duration.ofSeconds(ttlSeconds);
    }

    /** Marca 'processing' si no está DONE */
    public Mono<Boolean> tryLockProcessing(String key) {
        String k = "idem:" + key + ":lock";
        return redis.opsForValue().setIfAbsent(k, "processing", ttl).map(Boolean::booleanValue);
    }

    public Mono<Void> markDone(String key){ return redis.opsForValue().set("idem:"+key, "DONE", ttl).then(); }
}