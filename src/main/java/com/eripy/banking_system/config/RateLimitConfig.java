package com.eripy.banking_system.config;

import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.github.bucket4j.BucketConfiguration;

@Configuration 
public class RateLimitConfig {
    @Bean 
    public BucketConfiguration authConfiguration() {
        return BucketConfiguration
            .builder()
            .addLimit(limit ->
                limit.capacity(1)
                .refillGreedy(1, Duration.ofSeconds(15))
            )
            .build();
    }

    @Bean 
    public BucketConfiguration transactionConfiguration() {
        return BucketConfiguration
            .builder()
            .addLimit(limit ->
                limit.capacity(7)
                .refillGreedy(3, Duration.ofMinutes(10))
            )
            .build();
    }
}
