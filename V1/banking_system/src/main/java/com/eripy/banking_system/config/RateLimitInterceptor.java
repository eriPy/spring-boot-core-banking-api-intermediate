package com.eripy.banking_system.config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.springframework.web.servlet.HandlerInterceptor;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class RateLimitInterceptor implements HandlerInterceptor {
    private final BucketConfiguration bucketConfiguration;
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public RateLimitInterceptor(
        BucketConfiguration bucketConfiguration
    ) {
        this.bucketConfiguration = bucketConfiguration;
    }

    @Override 
    public boolean preHandle(
        HttpServletRequest request, 
        HttpServletResponse response,   
        Object handler
    ) throws Exception {
        String client = request.getRemoteAddr();
        Bucket bucket = buckets.computeIfAbsent(
            client, 
            k -> Bucket
                .builder()
                .addLimit(bucketConfiguration.getBandwidths()[0])
                .build()
        );
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        if (probe.isConsumed()) {
            response.addHeader(
                "X-Rate-Limit-Remaining", 
                String.valueOf(probe.getRemainingTokens()
            ));
            return true;
        }
        long seconds = Math.max(
            1, 
            TimeUnit.NANOSECONDS
                .toSeconds(probe.getNanosToWaitForRefill())
        );
        response.setStatus(429);
        response.addHeader(
            "Retry-After", 
            String.valueOf(seconds)
        );
        response.getWriter()
            .write("Too many requests, try again in: " + seconds + " seconds");
        return false;
    }
}
