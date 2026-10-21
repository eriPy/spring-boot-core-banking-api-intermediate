package com.eripy.banking_system.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.eripy.banking_system.event.UserBehaviorAnalyzedEvent;

@Component 
public class SmurfingDetectionConsumer {
    private static final Logger logger = LoggerFactory.getLogger(SmurfingDetectionConsumer.class);

    @Transactional 
    @KafkaListener(topics = "user-analized", groupId = "smurffing")
    public void analyzingSmurffing(UserBehaviorAnalyzedEvent event) {
        logger.info("Evaluating transactions from the last minute");
    } 
}
