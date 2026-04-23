package com.evmonitor.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, StatusChangeEvent> kafkaTemplate;

    private static final String TOPIC = "ev.processed.status";

    public void publishStatusChange(StatusChangeEvent event) {
        
        // Use borough as the Kafka key → same borough = same partition
        String key = event.getLondonBorough();

        CompletableFuture<SendResult<String, StatusChangeEvent>> future =
                kafkaTemplate.send(TOPIC, key, event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish Kafka event for station {}: {}",
                        event.getStationUuid(), ex.getMessage());
            } else {
                log.debug("Published status change for station {} → {} (partition {})",
                        event.getStationUuid(),
                        event.getNewStatus(),
                        result.getRecordMetadata().partition());
            }
        });
    }
}