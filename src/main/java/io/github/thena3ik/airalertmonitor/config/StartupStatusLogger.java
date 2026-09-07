package io.github.thena3ik.airalertmonitor.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class StartupStatusLogger {

    @Value("${air-alert.polling.enabled:true}")
    private String pollingEnabledRaw;

    @Value("${air-alert.webhook.require-https:true}")
    private boolean webhookRequireHttps;

    @Value("${air-alert.ubilling.source}")
    private String primarySource;

    @Value("${air-alert.ubilling.fallback-sources:}")
    private List<String> fallbackSources;

    @Value("${air-alert.polling.confirmation-polls:2}")
    private int confirmationPolls;

    @PostConstruct
    public void logStartupStatus() {
        logPollingStatus();
        logWebhookHttpsStatus();
        logConfirmationPolls();
        log.info("Ubilling primary source '{}', fallback chain: {}", primarySource,
                fallbackSources.isEmpty() ? "none" : fallbackSources);
    }

    private void logConfirmationPolls() {
        int effective = Math.max(1, confirmationPolls);
        if (effective == 1) {
            log.info("Alert transitions applied immediately (confirmation-polls={}, debounce disabled)", confirmationPolls);
        } else {
            log.info("Alert transitions require {} consecutive confirming polls before applying", effective);
        }
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