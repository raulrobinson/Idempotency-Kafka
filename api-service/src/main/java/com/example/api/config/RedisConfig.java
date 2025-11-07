package com.example.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {
    @Bean
    public ReactiveRedisConnectionFactory redisConnectionFactory() { return new LettuceConnectionFactory(); }

    @Bean
    public ReactiveStringRedisTemplate reactiveStringRedisTemplate(ReactiveRedisConnectionFactory cf) {
        var template = new ReactiveStringRedisTemplate(cf);
        template.setKeySerializer(StringRedisSerializer.UTF_8);
        template.setValueSerializer(StringRedisSerializer.UTF_8);
        return template;
    }
}