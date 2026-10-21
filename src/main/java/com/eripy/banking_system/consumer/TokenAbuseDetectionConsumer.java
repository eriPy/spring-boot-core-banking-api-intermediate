package com.eripy.banking_system.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.eripy.banking_system.event.UserBehaviorAnalyzedEvent;

@Component 
public class TokenAbuseDetectionConsumer {
    private static final Logger logger = LoggerFactory.getLogger(TokenAbuseDetectionConsumer.class);

    @Transactional 
    @KafkaListener(topics = "user-analized", groupId = "tokens")
    public void analyzingTokens(UserBehaviorAnalyzedEvent event) {
        logger.info("Evaluating tokens from the last 15 minutes");
    }
}
