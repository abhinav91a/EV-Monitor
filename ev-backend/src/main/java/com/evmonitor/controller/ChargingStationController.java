package com.evmonitor.controller;

import com.evmonitor.model.ChargingStation;
import com.evmonitor.service.ChargingStationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/stations")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ChargingStationController {

    private final ChargingStationService service;

    // GET all stations
    @GetMapping
    public List<ChargingStation> getAllStations() {
        return service.getAllStations();
    }

    // GET available stations only
    @GetMapping("/available")
    public List<ChargingStation> getAvailableStations() {
        return service.getAvailableStations();
    }

    // GET stations by borough
    @GetMapping("/borough/{borough}")
    public List<ChargingStation> getByBorough(
            @PathVariable String borough) {
        return service.getByBorough(borough);
    }

    // GET stations by status
    @GetMapping("/status/{status}")
    public List<ChargingStation> getByStatus(
            @PathVariable String status) {
        return service.getByStatus(status);
    }

    // POST add a new station
    @PostMapping
    public ResponseEntity<ChargingStation> addStation(
            @RequestBody ChargingStation station) {
        return ResponseEntity.ok(service.saveStation(station));
    }

    // POST reset all statuses — temporary, remove after use
    @PostMapping("/reset-status")
    public ResponseEntity<String> resetAllStatuses() {
        service.resetAllStatuses();
        return ResponseEntity.ok("All stations reset to UNKNOWN");
    }
}