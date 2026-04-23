package com.evmonitor.repository;

import com.evmonitor.model.ChargingStation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChargingStationRepository
        extends JpaRepository<ChargingStation, UUID> {

    List<ChargingStation> findByCurrentStatus(String status);
    List<ChargingStation> findByLondonBorough(String borough);
    Optional<ChargingStation> findByOcmUuid(String ocmUuid);
    boolean existsByOcmUuid(String ocmUuid);
}