package io.github.thena3ik.airalertmonitor.dto.webhook;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Request body for registering a new webhook subscription")
public record CreateWebhookRequest(
        @Schema(description = "Destination URL to receive event payloads. HTTPS is strictly required.", example = "https://example.com/hooks/alerts") String url,
        @Schema(description = "Ids of regions to subscribe to") List<Long> regionIds) {
}