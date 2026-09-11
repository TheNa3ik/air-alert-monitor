package io.github.thena3ik.airalertmonitor.dto.webhook;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Request body for replacing the regions a subscription listens to")
public record UpdateWebhookRegionsRequest(
        @Schema(description = "New full set of region ids for this subscription") List<Long> regionIds) {
}