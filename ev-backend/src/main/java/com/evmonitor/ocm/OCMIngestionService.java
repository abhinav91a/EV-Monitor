package com.evmonitor.ocm;

import com.evmonitor.model.ChargingStation;
import com.evmonitor.repository.ChargingStationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OCMIngestionService {

    private final OCMClient ocmClient;
    private final ChargingStationRepository repository;

    @Scheduled(initialDelay = 3600000, fixedRate = 86400000) // 1 hour delay
    public void ingestStations() {
        log.info("Starting OpenChargeMap ingestion...");

        List<OCMStation> rawStations = ocmClient.fetchLondonStations();
        int saved = 0;
        int skipped = 0;

        for (OCMStation raw : rawStations) {
            try {
                saveOrUpdate(raw);
                saved++;
            } catch (Exception e) {
                log.warn("Skipping station {} due to error: {}",
                        raw.getUuid(), e.getMessage());
                skipped++;
            }
        }

        log.info("Ingestion complete. Saved: {}, Skipped: {}", saved, skipped);
    }

    private void saveOrUpdate(OCMStation raw) {
        boolean isNew = !repository.existsByOcmUuid(raw.getUuid());

        ChargingStation station = repository
                .findByOcmUuid(raw.getUuid())
                .orElse(new ChargingStation());

        mapToStation(raw, station);

        // Only set status for brand new stations — TomTom owns live status
        if (isNew) {
            station.setCurrentStatus("UNKNOWN");
        }

        repository.save(station);
    }

    private void mapToStation(OCMStation raw, ChargingStation station) {
        station.setOcmUuid(raw.getUuid());
        station.setLastUpdated(Instant.now());

        if (raw.getAddressInfo() != null) {
            station.setLatitude(raw.getAddressInfo().getLatitude());
            station.setLongitude(raw.getAddressInfo().getLongitude());
            station.setPostcode(raw.getAddressInfo().getPostcode());
            station.setLondonBorough(
                    raw.getAddressInfo().getTown() != null
                            ? raw.getAddressInfo().getTown()
                            : "London"
            );
        }

        if (raw.getOperatorInfo() != null
                && raw.getOperatorInfo().getTitle() != null) {
            station.setOperatorName(raw.getOperatorInfo().getTitle());
        } else {
            station.setOperatorName("Independent");
        }

        if (raw.getConnections() != null) {
            int total = raw.getConnections().stream()
                    .mapToInt(c -> c.getQuantity() != null ? c.getQuantity() : 1)
                    .sum();
            station.setTotalConnectors(total);
        }

        // No status logic here — TomTom owns live status
    }
}