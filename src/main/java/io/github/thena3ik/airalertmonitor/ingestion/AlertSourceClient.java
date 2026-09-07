package io.github.thena3ik.airalertmonitor.ingestion;

import io.github.thena3ik.airalertmonitor.dto.ubilling.UbillingAlertsResponse;

public interface AlertSourceClient {
    UbillingAlertsResponse fetchCurrentStates();
}
