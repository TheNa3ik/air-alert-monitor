package io.github.thena3ik.airalertmonitor.dto.webhook;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "api.model.createWebhook.desc")
public record CreateWebhookRequest(
        @Schema(description = "api.model.createWebhook.url", example = "https://example.com/hooks/alerts") String url,
        @Schema(description = "api.model.createWebhook.regionIds") List<Long> regionIds) {
}