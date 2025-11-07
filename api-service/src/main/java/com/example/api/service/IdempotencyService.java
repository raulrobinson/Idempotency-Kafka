package com.example.api.service;

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

    // Devuelve true si pudo registrar (no existía)
    public Mono<Boolean> tryRegister(String key) {
        String redisKey = "idem:" + key;
        return redis.opsForValue().setIfAbsent(redisKey, "PENDING", ttl)
                .map(Boolean::booleanValue);
    }

    public Mono<Void> markDone(String key) {
        return redis.opsForValue().set("idem:"+key, "DONE", ttl).then();
    }
}