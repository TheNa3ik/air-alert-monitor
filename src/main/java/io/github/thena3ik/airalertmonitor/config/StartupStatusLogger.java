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

    @PostConstruct
    public void logPollingStatus() {
        boolean enabled = !pollingEnabledRaw.trim().equalsIgnoreCase("false");
        log.info("Polling scheduler is {}", enabled
                ? "enabled"
                : "disabled (set POLLING_ENABLED=false to disable, unset/true to enable)");
    }
}