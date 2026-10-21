package com.eripy.banking_system.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.eripy.banking_system.event.UserBehaviorAnalyzedEvent;

@Component 
public class FlaggedDetectionConsumer {
    private static final Logger logger = LoggerFactory.getLogger(FlaggedDetectionConsumer.class);

    @Transactional 
    @KafkaListener(topics = "user-analized", groupId = "flagged")
    public void analizingFlagged(UserBehaviorAnalyzedEvent event) {
        logger.info("Evaluating the last transactions from the last 3 months");
    }
}
