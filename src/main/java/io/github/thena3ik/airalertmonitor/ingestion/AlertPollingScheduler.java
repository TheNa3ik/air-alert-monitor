package io.github.thena3ik.airalertmonitor.ingestion;

import io.github.thena3ik.airalertmonitor.dto.ubilling.UbillingAlertsResponse;
import io.github.thena3ik.airalertmonitor.service.AlertDiffingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnExpression("!'${air-alert.polling.enabled:true}'.equalsIgnoreCase('false')")
public class AlertPollingScheduler {

    private final UbillingAlertsClient ubillingAlertsClient;
    private final AlertDiffingService alertDiffingService;

    @Scheduled(fixedDelay = 10000)
    public void checkAlerts() {

        UbillingAlertsResponse response;

        try {
            response = ubillingAlertsClient.fetchCurrentStates();
            alertDiffingService.processPoll(response);
        } catch (Exception exc) {
            log.warn("Failed to poll/process alerts", exc);
        }
    }
}
