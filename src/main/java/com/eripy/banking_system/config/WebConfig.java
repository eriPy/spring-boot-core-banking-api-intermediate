package com.eripy.banking_system.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import io.github.bucket4j.BucketConfiguration;

@Configuration  
public class WebConfig implements WebMvcConfigurer{
    private final BucketConfiguration authConfig;
    private final BucketConfiguration transactionConfig;

    public WebConfig(
        @Qualifier("authConfiguration") BucketConfiguration authConfig,
        @Qualifier("transactionConfiguration") BucketConfiguration transactionConfig
    ) {
        this.authConfig = authConfig;
        this.transactionConfig = transactionConfig;
    }

    @Override 
    public void addInterceptors(InterceptorRegistry registry) {
        registry   
            .addInterceptor(new RateLimitInterceptor(authConfig))
            .addPathPatterns("/api/auth/**");

        registry
            .addInterceptor(new RateLimitInterceptor(transactionConfig))
            .addPathPatterns(
                "/api/account/token"
        );
    }
}