package com.evmonitor.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatusChangeEvent {
    

    private String  stationUuid;          // Which station changed
    private String  oldStatus;            // What it was before
    private String  newStatus;            // What it is now
    private String  londonBorough;        // For Kafka partitioning
    private Integer availableConnectors;  // How many free right now
    private Integer totalConnectors;      // Total connectors at site
    private Instant changedAt;            // When the change happened
}