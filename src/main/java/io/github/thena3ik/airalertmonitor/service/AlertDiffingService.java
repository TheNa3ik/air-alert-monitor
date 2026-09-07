package io.github.thena3ik.airalertmonitor.service;

import io.github.thena3ik.airalertmonitor.dto.ubilling.UbillingAlertsResponse;
import io.github.thena3ik.airalertmonitor.dto.webhook.WebhookEventPayload;
import io.github.thena3ik.airalertmonitor.entity.AlertEvent;
import io.github.thena3ik.airalertmonitor.entity.EventType;
import io.github.thena3ik.airalertmonitor.entity.Region;
import io.github.thena3ik.airalertmonitor.entity.WebhookSubscription;
import io.github.thena3ik.airalertmonitor.notification.WebhookDeliveryClient;
import io.github.thena3ik.airalertmonitor.repository.AlertEventRepository;
import io.github.thena3ik.airalertmonitor.repository.RegionRepository;
import io.github.thena3ik.airalertmonitor.repository.WebhookSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertDiffingService {

    private final RegionRepository regionRepository;
    private final AlertEventRepository alertEventRepository;
    private final WebhookSubscriptionRepository webhookSubscriptionRepository;
    private final WebhookDeliveryClient webhookDeliveryClient;
    private final AlertTransitionDebouncer transitionDebouncer;

    @Transactional
    public void processPoll(UbillingAlertsResponse response) {
        for (var entry : response.states().entrySet()) {
            String regionName = entry.getKey();
            boolean alertNowActive = entry.getValue().alertnow();

            Optional<Region> maybeRegion = regionRepository.findByName(regionName);
            if (maybeRegion.isEmpty()) {
                log.warn("Unknown region from API: {}, skipping", regionName);
                continue;
            }
            Region region = maybeRegion.get();

            Optional<AlertEvent> openEvent = alertEventRepository.findByRegionAndEndedAtIsNull(region);
            boolean confirmedActive = openEvent.isPresent();

            if (alertNowActive == confirmedActive) {
                transitionDebouncer.clear(region.getId());
                continue;
            }

            Optional<Instant> confirmedAt = transitionDebouncer.confirm(region.getId(), alertNowActive);
            if (confirmedAt.isEmpty()) {
                log.debug("Region {} candidate alertnow={} not yet confirmed", regionName, alertNowActive);
                continue;
            }

            Instant occurredAt = confirmedAt.get();

            if (alertNowActive) {
                AlertEvent newEvent = alertEventRepository.save(new AlertEvent(region, occurredAt, response.source()));
                notifySubscribers(region, EventType.ALERT_STARTED, newEvent.getStartedAt());
            } else {
                AlertEvent eventToClose = openEvent.get();
                eventToClose.close(occurredAt);
                alertEventRepository.save(eventToClose);
                notifySubscribers(region, EventType.ALERT_ENDED, eventToClose.getStartedAt());
            }
        }
    }

    private void notifySubscribers(Region region, EventType eventType, Instant occurredAt) {
        List<WebhookSubscription> subscriptions = webhookSubscriptionRepository.findByRegionsContainingAndActiveTrue(region);
        if (subscriptions.isEmpty()) {
            return;
        }

        WebhookEventPayload payload = new WebhookEventPayload(eventType, region.getId(), region.getName(), occurredAt);
        for (WebhookSubscription subscription : subscriptions) {
            webhookDeliveryClient.deliver(subscription, payload);
        }
    }
}
