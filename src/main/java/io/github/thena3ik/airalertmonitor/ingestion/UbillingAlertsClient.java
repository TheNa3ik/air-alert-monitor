package io.github.thena3ik.airalertmonitor.ingestion;

import io.github.thena3ik.airalertmonitor.dto.UbillingAlertsResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class UbillingAlertsClient {

    private static final String JSON_URL = "https://ubilling.net.ua/aerialalerts/";
    private final RestClient restClient;

    public UbillingAlertsClient(RestClient.Builder restClientBuilder) {
        restClient = restClientBuilder
                .baseUrl(JSON_URL)
                .defaultHeader("Accept", "application/json")
                .build();
    }

    public UbillingAlertsResponse fetchCurrentStates() {
        return restClient
                .get()
                .retrieve()
                .body(UbillingAlertsResponse.class);
    }

}
