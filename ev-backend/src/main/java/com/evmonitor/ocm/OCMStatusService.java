package com.evmonitor.ocm;

import com.evmonitor.kafka.KafkaProducerService;
import com.evmonitor.kafka.StatusChangeEvent;
import com.evmonitor.model.ChargingStation;
import com.evmonitor.repository.ChargingStationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OCMStatusService {

    private final ChargingStationRepository repository;
    private final KafkaProducerService kafkaProducer;
    private final WebClient.Builder webClientBuilder;

    @Value("${ocm.api.key}")
    private String apiKey;

    // Runs every 5 minutes — checks OCM for status updates
    @Scheduled(initialDelay = 60000, fixedRate = 300000)
    public void refreshStatuses() {
        log.info("OCM status refresh started...");

        List<ChargingStation> stations = repository.findAll();
        int updated = 0;

        for (ChargingStation station : stations) {
            try {
                // Fetch latest data for this specific station by UUID
                OCMStation[] result = webClientBuilder.build()
                        .get()
                        .uri("https://api.openchargemap.io/v3/poi", u -> u
                                .queryParam("key", apiKey)
                                .queryParam("chargepointid", station.getOcmUuid())
                                .queryParam("compact", false)
                                .queryParam("maxresults", 1)
                                .build())
                        .retrieve()
                        .bodyToMono(OCMStation[].class)
                        .block();

                if (result == null || result.length == 0) continue;

                OCMStation fresh = result[0];
                String newStatus = deriveStatus(fresh);
                String oldStatus = station.getCurrentStatus();

                // Update operator name if now available
                if (fresh.getOperatorInfo() != null
                        && fresh.getOperatorInfo().getTitle() != null) {
                    station.setOperatorName(
                            fresh.getOperatorInfo().getTitle());
                }

                if (!newStatus.equals(oldStatus)) {
                    station.setCurrentStatus(newStatus);
                    station.setLastUpdated(Instant.now());
                    repository.save(station);
                    updated++;

                    kafkaProducer.publishStatusChange(new StatusChangeEvent(
                            station.getOcmUuid(),
                            oldStatus,
                            newStatus,
                            station.getLondonBorough(),
                            station.getAvailableConnectors(),
                            station.getTotalConnectors(),
                            Instant.now()
                    ));

                    log.info("Status updated: {} → {} → {}",
                            station.getOcmUuid(), oldStatus, newStatus);
                }

                // Small delay to avoid rate limiting
                Thread.sleep(200);

            } catch (Exception e) {
                log.warn("Failed to refresh station {}: {}",
                        station.getOcmUuid(), e.getMessage());
            }
        }

        log.info("OCM status refresh complete. {} updated.", updated);
    }

    private String deriveStatus(OCMStation station) {
        if (station.getStatusType() == null) return "UNKNOWN";
        if (Boolean.TRUE.equals(
                station.getStatusType().getIsOperational())) return "AVAILABLE";
        if (Boolean.FALSE.equals(
                station.getStatusType().getIsOperational())) return "FAULTY";
        return "UNKNOWN";
    }
}