package com.evmonitor.service;

import com.evmonitor.model.ChargingStation;
import com.evmonitor.repository.ChargingStationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChargingStationService {

    private final ChargingStationRepository repository;

    // Get all stations
    public List<ChargingStation> getAllStations() {
        return repository.findAll().stream()
                .filter(s -> s.getTotalConnectors() != null
                        && s.getTotalConnectors() > 0)
                .collect(Collectors.toList());
    }

    // Get only available stations
    public List<ChargingStation> getAvailableStations() {
        return repository.findByCurrentStatus("AVAILABLE");
    }

    // Get by borough
    public List<ChargingStation> getByBorough(String borough) {
        return repository.findByLondonBorough(borough);
    }

    // Get by status
    public List<ChargingStation> getByStatus(String status) {
        return repository.findByCurrentStatus(status);
    }

    // Save a single station
    public ChargingStation saveStation(ChargingStation station) {
        return repository.save(station);
    }

    public void resetAllStatuses() {
        repository.findAll().forEach(station -> {
            station.setCurrentStatus("UNKNOWN");
            repository.save(station);
        });
    }
}