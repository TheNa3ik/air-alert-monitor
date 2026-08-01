package io.github.thena3ik.airalertmonitor.dto;

import java.util.Map;

public record  UbillingAlertsResponse(
        String source,
        String cachedat,
        Map<String, RegionState> states) {
}
