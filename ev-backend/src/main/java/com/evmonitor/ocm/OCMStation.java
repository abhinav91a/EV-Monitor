package com.evmonitor.ocm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)  // Ignore fields we don't need
public class OCMStation {

    @JsonProperty("ID")
    private Integer id;

    @JsonProperty("UUID")
    private String uuid;

    @JsonProperty("AddressInfo")
    private AddressInfo addressInfo;

    @JsonProperty("Connections")
    private List<Connection> connections;

    @JsonProperty("OperatorInfo")
    private OperatorInfo operatorInfo;

    @JsonProperty("StatusType")
    private StatusType statusType;

    // ── Inner classes (mirrors OCM JSON structure) ──────────────────

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AddressInfo {
        @JsonProperty("Title")
        private String title;

        @JsonProperty("AddressLine1")
        private String addressLine1;

        @JsonProperty("Postcode")
        private String postcode;

        @JsonProperty("Latitude")
        private Double latitude;

        @JsonProperty("Longitude")
        private Double longitude;

        @JsonProperty("Town")
        private String town;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Connection {
        @JsonProperty("PowerKW")
        private Integer powerKw;

        @JsonProperty("Quantity")
        private Integer quantity;
    }
    

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OperatorInfo {
        @JsonProperty("Title")
        private String title;

        @JsonProperty("PhonePrimaryContact")
        private String phone;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StatusType {
        @JsonProperty("IsOperational")
        private Boolean isOperational;

        @JsonProperty("Title")
        private String title;
    }
}