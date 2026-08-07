package io.github.thena3ik.airalertmonitor.dto.ubilling;

import java.util.Map;

public record  UbillingAlertsResponse(
        String source,
        String cachedat,
        Map<String, RegionState> states) {
}
