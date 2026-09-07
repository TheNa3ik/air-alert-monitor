package io.github.thena3ik.airalertmonitor.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class StartupStatusLogger {

    @Value("${air-alert.polling.enabled:true}")
    private String pollingEnabledRaw;

    @Value("${air-alert.webhook.require-https:true}")
    private boolean webhookRequireHttps;

    @PostConstruct
    public void logStartupStatus() {
        logPollingStatus();
        logWebhookHttpsStatus();
    }

    private void logPollingStatus() {
        boolean enabled = !pollingEnabledRaw.trim().equalsIgnoreCase("false");
        log.info("Polling scheduler is {}", enabled
                ? "enabled"
                : "disabled (set POLLING_ENABLED=false to disable, unset/true to enable)");
    }

    private void logWebhookHttpsStatus() {
        if (webhookRequireHttps) {
            log.info("Webhook URL validation requires HTTPS");
        } else {
            log.warn("Webhook URL validation allows plain HTTP (WEBHOOK_REQUIRE_HTTPS=false) "
                    + "— this should only be used in local/dev environments");
        }
    }
}