package io.github.thena3ik.airalertmonitor.ingestion;

import io.github.thena3ik.airalertmonitor.dto.ubilling.UbillingAlertsResponse;
import io.github.thena3ik.airalertmonitor.exception.UbillingPollException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class UbillingAlertsClient implements AlertSourceClient {

    private static final String BASE_URL = "https://ubilling.net.ua/aerialalerts/";

    private final RestClient restClient;
    private final List<String> sources; // index 0 = primary, rest = ordered fallbacks
    private final int failureThreshold;
    private final int recoveryCheckPolls;

    private volatile int activeIndex = 0;
    private final AtomicInteger consecutiveFailures = new AtomicInteger(0);
    private final AtomicInteger pollsSinceRecoveryCheck = new AtomicInteger(0);

    public UbillingAlertsClient(RestClient.Builder restClientBuilder,
                                @Value("${air-alert.ubilling.source}") String primarySource,
                                @Value("${air-alert.ubilling.fallback-sources:}") List<String> fallbackSources,
                                @Value("${air-alert.ubilling.failure-threshold:3}") int failureThreshold,
                                @Value("${air-alert.ubilling.recovery-check-polls:12}") int recoveryCheckPolls) {
        this.restClient = restClientBuilder
                .baseUrl(BASE_URL)
                .defaultHeader("Accept", "application/json")
                .build();

        List<String> ordered = new ArrayList<>();
        ordered.add(primarySource);
        ordered.addAll(fallbackSources);
        this.sources = List.copyOf(ordered);

        this.failureThreshold = Math.max(1, failureThreshold);
        this.recoveryCheckPolls = Math.max(1, recoveryCheckPolls);
    }

    @Override
    public UbillingAlertsResponse fetchCurrentStates() {
        if (activeIndex != 0 && pollsSinceRecoveryCheck.incrementAndGet() >= recoveryCheckPolls) {
            pollsSinceRecoveryCheck.set(0);
            UbillingAlertsResponse recovered = tryRecoverPrimary();
            if (recovered != null) {
                return recovered;
            }
        }

        try {
            UbillingAlertsResponse response = fetchFrom(sources.get(activeIndex));
            consecutiveFailures.set(0);
            return response;
        } catch (Exception exc) {
            return handleFailureAndRetryNext(exc);
        }
    }

    private UbillingAlertsResponse tryRecoverPrimary() {
        try {
            UbillingAlertsResponse response = fetchFrom(sources.getFirst());
            log.info("Primary ubilling source '{}' recovered, switching back from '{}'",
                    sources.getFirst(), sources.get(activeIndex));
            activeIndex = 0;
            consecutiveFailures.set(0);
            return response;
        } catch (Exception exc) {
            log.debug("Primary ubilling source '{}' still unavailable, staying on '{}'",
                    sources.getFirst(), sources.get(activeIndex));
            return null;
        }
    }

    private UbillingAlertsResponse handleFailureAndRetryNext(Exception cause) {
        int failures = consecutiveFailures.incrementAndGet();
        log.warn("Poll failed on ubilling source '{}' ({} consecutive failure(s))",
                sources.get(activeIndex), failures, cause);

        if (failures < failureThreshold || sources.size() == 1) {
            throw new UbillingPollException("Poll failed on source '" + sources.get(activeIndex) + "'");
        }

        int nextIndex = (activeIndex + 1) % sources.size();
        log.warn("Switching active ubilling source from '{}' to '{}' after {} consecutive failures",
                sources.get(activeIndex), sources.get(nextIndex), failures);
        activeIndex = nextIndex;
        consecutiveFailures.set(0);
        pollsSinceRecoveryCheck.set(0);

        return fetchFrom(sources.get(activeIndex));
    }

    private UbillingAlertsResponse fetchFrom(String source) {
        return restClient
                .get()
                .uri(uriBuilder -> uriBuilder.queryParam("source", source).build())
                .retrieve()
                .body(UbillingAlertsResponse.class);
    }
}