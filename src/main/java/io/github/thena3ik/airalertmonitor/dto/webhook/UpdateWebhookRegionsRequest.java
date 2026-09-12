package io.github.thena3ik.airalertmonitor.dto.webhook;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "api.model.updateWebhookRegions.desc")
public record UpdateWebhookRegionsRequest(
        @Schema(description = "api.model.updateWebhookRegions.regionIds") List<Long> regionIds) {
}