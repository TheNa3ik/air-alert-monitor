package io.github.thena3ik.airalertmonitor.ingestion;

import io.github.thena3ik.airalertmonitor.dto.ubilling.UbillingAlertsResponse;
import io.github.thena3ik.airalertmonitor.service.AlertDiffingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnExpression("!'${air-alert.polling.enabled:true}'.equalsIgnoreCase('false')")
@RequiredArgsConstructor
@Slf4j
public class AlertPollingScheduler {

    private final AlertSourceClient alertSourceClient;
    private final AlertDiffingService alertDiffingService;

    @Scheduled(fixedDelayString = "${air-alert.polling.interval-ms:5000}")
    public void checkAlerts() {
        try {
            UbillingAlertsResponse response = alertSourceClient.fetchCurrentStates();
            alertDiffingService.processPoll(response);
        } catch (Exception exc) {
            log.warn("Failed to poll/process alerts", exc);
        }
    }
}