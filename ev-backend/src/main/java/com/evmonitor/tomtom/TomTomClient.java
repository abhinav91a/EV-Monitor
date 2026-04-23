package com.evmonitor.tomtom;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class TomTomClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${tomtom.api.key}")
    private String apiKey;

    @Value("${tomtom.api.url}")
    private String apiUrl;

    // Fetch live status for a list of OCM UUIDs
    public Optional<TomTomResponse> fetchAvailability(List<String> ocmUuids) {
        if (ocmUuids.isEmpty()) return Optional.empty();

        // TomTom accepts a comma-separated list of IDs
        String ids = String.join(",", ocmUuids);

        try {
            TomTomResponse response = webClientBuilder
                    .build()
                    .get()
                    .uri(apiUrl, uriBuilder -> uriBuilder
                            .queryParam("key", apiKey)
                            .queryParam("chargingAvailability", ids)
                            .build())
                    .retrieve()
                    .bodyToMono(TomTomResponse.class)
                    .block();

            return Optional.ofNullable(response);

        } catch (WebClientResponseException.TooManyRequests e) {
            // 429 — we hit the rate limit, skip this cycle
            log.warn("TomTom rate limit hit. Skipping this poll cycle.");
            return Optional.empty();

        } catch (WebClientResponseException.ServiceUnavailable e) {
            // 503 — TomTom is down, skip this cycle
            log.error("TomTom API unavailable. Skipping this poll cycle.");
            return Optional.empty();

        } catch (Exception e) {
            log.error("Unexpected error calling TomTom API: {}", e.getMessage());
            return Optional.empty();
        }
    }
}