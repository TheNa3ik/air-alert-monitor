package io.github.thena3ik.airalertmonitor.dto.webhook;

import java.util.List;

public record CreateWebhookRequest(
        String url,
        List<Long> regionIds) {
}
