package com.evmonitor.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Data
@Entity
@Table(name = "charging_stations")
public class ChargingStation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "ocm_uuid", unique = true)
    private String ocmUuid;

    private String operatorName;
    private Double latitude;
    private Double longitude;
    private String postcode;
    private String londonBorough;
    private Integer totalConnectors;
    private Integer availableConnectors;

    @Column(name = "current_status")
    private String currentStatus = "UNKNOWN";

    @Column(name = "last_updated")
    private Instant lastUpdated = Instant.now();
}