package com.evmonitor.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumerService {

    private final SimpMessagingTemplate messagingTemplate;

    @KafkaListener(
            topics = "ev.processed.status",
            groupId = "ev-processing-group"
    )
    public void onStatusChange(StatusChangeEvent event) {
        log.info("⚡ STATUS CHANGE: {} → {}", event.getStationUuid(), event.getNewStatus());

        // Push to Angular via WebSocket
        messagingTemplate.convertAndSend("/topic/status", event);
    }
}