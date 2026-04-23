package com.evmonitor.ocm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OCMClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${ocm.api.key}")
    private String apiKey;

    @Value("${ocm.api.url}")
    private String apiUrl;

    @Value("${ocm.api.latitude}")
    private double latitude;

    @Value("${ocm.api.longitude}")
    private double longitude;

    @Value("${ocm.api.radius-km}")
    private int radiusKm;

    @Value("${ocm.api.max-results}")
    private int maxResults;

    public List<OCMStation> fetchLondonStations() {
        log.info("Fetching stations from OpenChargeMap API...");

        List<OCMStation> stations = webClientBuilder
                .build()
                .get()
                .uri(apiUrl, uriBuilder -> uriBuilder
                        .queryParam("key",        apiKey)
                        .queryParam("latitude",   latitude)
                        .queryParam("longitude",  longitude)
                        .queryParam("distance",   radiusKm)
                        .queryParam("distanceunit", "KM")
                        .queryParam("maxresults", maxResults)
                        .queryParam("countrycode", "GB")
                        .queryParam("verbose",    false)
                        .queryParam("compact", false)
                        .queryParam("includecomments", false)
                        .build())
                .retrieve()
                .bodyToFlux(OCMStation.class)
                .collectList()
                .block();                              // Wait for response (simple for now)

        log.info("Fetched {} stations from OpenChargeMap",
                stations != null ? stations.size() : 0);
        return stations != null ? stations : List.of();
    }
}