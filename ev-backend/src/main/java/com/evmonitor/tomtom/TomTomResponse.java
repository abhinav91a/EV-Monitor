package com.evmonitor.tomtom;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TomTomResponse {

    @JsonProperty("connectorAvailabilities")
    private List<ConnectorAvailability> connectorAvailabilities;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ConnectorAvailability {

        @JsonProperty("id")
        private String id;          // This matches OCM UUID

        @JsonProperty("availability")
        private Availability availability;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Availability {

        @JsonProperty("current")
        private CurrentStatus current;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CurrentStatus {

        @JsonProperty("available")
        private Integer available;     // Number of available connectors

        @JsonProperty("occupied")
        private Integer occupied;      // Number of occupied connectors

        @JsonProperty("outOfService")
        private Integer outOfService;  // Number of faulty connectors

        @JsonProperty("unknown")
        private Integer unknown;
    }
}