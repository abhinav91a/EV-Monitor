package com.evmonitor.tomtom;

import com.evmonitor.kafka.StatusChangeEvent;
import com.evmonitor.kafka.KafkaProducerService;
import com.evmonitor.model.ChargingStation;
import com.evmonitor.repository.ChargingStationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TomTomStatusService {

    private final TomTomClient tomTomClient;
    private final ChargingStationRepository repository;
    private final KafkaProducerService kafkaProducer;

    // Runs every 3 minutes after the app starts
    @Scheduled(initialDelay = 30000, fixedRateString = "${tomtom.api.poll-interval-ms}")
    public void pollAndUpdateStatuses() {
        log.info("TomTom poll cycle started...");

        // 1. Load all stations from our DB
        List<ChargingStation> allStations = repository.findAll();
        if (allStations.isEmpty()) {
            log.info("No stations in DB yet. Run OCM ingestion first.");
            return;
        }

        // 2. Extract OCM UUIDs to query TomTom
        //    Process in batches of 50 (TomTom API limit per request)
        List<String> uuids = allStations.stream()
                .map(ChargingStation::getOcmUuid)
                .filter(uuid -> uuid != null)
                .collect(Collectors.toList());

        // 3. Build a lookup map: UUID → Station (for fast access)
        Map<String, ChargingStation> stationMap = allStations.stream()
                .filter(s -> s.getOcmUuid() != null)
                .collect(Collectors.toMap(
                        s -> s.getOcmUuid().toLowerCase(),
                        s -> s,
                        (a, b) -> a  // Keep first if duplicates
                ));

        // 4. Process in batches of 50
        int batchSize = 50;
        int updated = 0;

        for (int i = 0; i < uuids.size(); i += batchSize) {
            List<String> batch = uuids.subList(i,
                    Math.min(i + batchSize, uuids.size()));

            var response = tomTomClient.fetchAvailability(batch);
            if (response.isEmpty()) continue;

            // 5. Process each result
            if (response.get().getConnectorAvailabilities() != null) {
                for (var availability : response.get().getConnectorAvailabilities()) {
                    ChargingStation station = stationMap.get(
                            availability.getId().toLowerCase());
                    if (station == null) continue;

                    String newStatus = deriveStatus(availability);
                    String oldStatus = station.getCurrentStatus();

                    if (availability.getAvailability() != null
                            && availability.getAvailability().getCurrent() != null) {
                        Integer avail = availability.getAvailability()
                                .getCurrent().getAvailable();
                        station.setAvailableConnectors(avail != null ? avail : 0);
                    }

                    // Only save + publish Kafka event if status actually changed
                    if (!newStatus.equals(oldStatus)) {
                        station.setCurrentStatus(newStatus);
                        station.setLastUpdated(Instant.now());

                        repository.save(station);
                        updated++;

                        kafkaProducer.publishStatusChange(
                                new StatusChangeEvent(
                                        station.getOcmUuid(),
                                        oldStatus != null ? oldStatus : "UNKNOWN",
                                        newStatus,
                                        station.getLondonBorough(),
                                        station.getAvailableConnectors(),
                                        station.getTotalConnectors(),
                                        Instant.now()
                                )
                        );

                        log.info("Station {} changed: {} → {}",
                                station.getOcmUuid(), oldStatus, newStatus);
                    }
                }
            }
        }

        log.info("TomTom poll complete. {} stations updated.", updated);
    }

    // Convert TomTom numbers into our status string
    private String deriveStatus(TomTomResponse.ConnectorAvailability availability) {
        if (availability.getAvailability() == null
                || availability.getAvailability().getCurrent() == null) {
            log.debug("Station {} has null availability — marking UNKNOWN",
                    availability.getId());
            return "UNKNOWN";
        }

        var current = availability.getAvailability().getCurrent();
        log.debug("Station {} — available:{} occupied:{} outOfService:{}",
                availability.getId(),
                current.getAvailable(),
                current.getOccupied(),
                current.getOutOfService());

        int available    = current.getAvailable()    != null ? current.getAvailable()    : 0;
        int outOfService = current.getOutOfService() != null ? current.getOutOfService() : 0;
        int occupied     = current.getOccupied()     != null ? current.getOccupied()     : 0;

        if (outOfService > 0 && available == 0 && occupied == 0) return "FAULTY";
        if (available > 0)                                        return "AVAILABLE";
        if (occupied > 0)                                         return "OCCUPIED";
        return "UNKNOWN";
    }
}