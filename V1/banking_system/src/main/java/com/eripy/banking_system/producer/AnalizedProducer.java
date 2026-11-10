package com.eripy.banking_system.producer;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import com.eripy.banking_system.event.UserBehaviorAnalyzedEvent;

@Component 
public class AnalizedProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public AnalizedProducer(
        KafkaTemplate<String, Object> kafkaTemplate
    ) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void userAnalized(UserBehaviorAnalyzedEvent event) {
        kafkaTemplate.send("user-analized", event);
    }
}
