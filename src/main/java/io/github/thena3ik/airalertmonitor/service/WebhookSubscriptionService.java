package io.github.thena3ik.airalertmonitor.service;

import io.github.thena3ik.airalertmonitor.dto.webhook.CreateWebhookRequest;
import io.github.thena3ik.airalertmonitor.dto.webhook.WebhookSubscriptionResponse;
import io.github.thena3ik.airalertmonitor.dto.webhook.WebhookSubscriptionSummaryResponse;
import io.github.thena3ik.airalertmonitor.entity.Region;
import io.github.thena3ik.airalertmonitor.entity.WebhookSubscription;
import io.github.thena3ik.airalertmonitor.exception.RegionNotFoundException;
import io.github.thena3ik.airalertmonitor.exception.WebhookNotFoundException;
import io.github.thena3ik.airalertmonitor.repository.RegionRepository;
import io.github.thena3ik.airalertmonitor.repository.WebhookSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookSubscriptionService {

    private final WebhookUrlValidator webhookUrlValidator;
    private final RegionRepository regionRepository;
    private final WebhookSubscriptionRepository webhookSubscriptionRepository;

    private static final int MAX_CONSECUTIVE_FAILURES = 5;

    @Transactional
    public void recordSuccess(Long subscriptionId) {
        webhookSubscriptionRepository.findById(subscriptionId).ifPresent(sub -> {
            sub.setConsecutiveFailures(0);
            webhookSubscriptionRepository.save(sub);
        });
    }

    @Transactional
    public void recordFailure(Long subscriptionId) {
        webhookSubscriptionRepository.findById(subscriptionId).ifPresent(sub -> {
            sub.setConsecutiveFailures(sub.getConsecutiveFailures() + 1);

            if (sub.getConsecutiveFailures() >= MAX_CONSECUTIVE_FAILURES) {
                sub.setActive(false);
                log.warn("Subscription {} deactivated after {} consecutive failures", subscriptionId, MAX_CONSECUTIVE_FAILURES);
            }

            webhookSubscriptionRepository.save(sub);
        });
    }

    public WebhookSubscriptionSummaryResponse getWebhook(Long id) {
        WebhookSubscription subscription = webhookSubscriptionRepository.findById(id)
                .orElseThrow(() -> new WebhookNotFoundException("Webhook not found: " + id));
        return toSummary(subscription);
    }

    public WebhookSubscriptionSummaryResponse updateRegions(Long id, List<Long> regionIds) {
        WebhookSubscription subscription = webhookSubscriptionRepository.findById(id)
                .orElseThrow(() -> new WebhookNotFoundException("Webhook not found: " + id));

        List<Region> regions = regionRepository.findAllById(regionIds);
        if (regions.isEmpty()) {
            throw new RegionNotFoundException("No valid regions found for provided ids");
        }

        subscription.setRegions(new HashSet<>(regions));
        webhookSubscriptionRepository.save(subscription);

        return toSummary(subscription);
    }

    public WebhookSubscriptionSummaryResponse reactivateWebhook(Long id) {
        WebhookSubscription subscription = webhookSubscriptionRepository.findById(id)
                .orElseThrow(() -> new WebhookNotFoundException("Webhook not found: " + id));

        subscription.setActive(true);
        subscription.setConsecutiveFailures(0);
        webhookSubscriptionRepository.save(subscription);

        return toSummary(subscription);
    }

    public void deleteWebhook(Long id) {
        if (!webhookSubscriptionRepository.existsById(id)) {
            throw new WebhookNotFoundException("Webhook not found: " + id);
        }
        webhookSubscriptionRepository.deleteById(id);
    }

    public WebhookSubscriptionResponse createWebhook(CreateWebhookRequest request) {
            webhookUrlValidator.validate(request.url());

        List<Region> regions = regionRepository.findAllById(request.regionIds());
        if (regions.isEmpty()) {
            throw new RegionNotFoundException("No valid regions found for provided ids");
        }

        String secret = generateSecret();
        WebhookSubscription subscription = new WebhookSubscription(request.url(), secret);
        subscription.setRegions(new HashSet<>(regions));

        webhookSubscriptionRepository.save(subscription);

        return toResponse(subscription);
    }

    private String generateSecret() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private List<Long> extractRegionIds(WebhookSubscription subscription) {
        return subscription.getRegions().stream().map(Region::getId).toList();
    }

    private WebhookSubscriptionResponse toResponse(WebhookSubscription subscription) {
        return new WebhookSubscriptionResponse(
                subscription.getId(),
                subscription.getUrl(),
                subscription.getSecret(),
                subscription.isActive(),
                extractRegionIds(subscription),
                subscription.getCreatedAt());
    }

    private WebhookSubscriptionSummaryResponse toSummary(WebhookSubscription subscription) {
        return new WebhookSubscriptionSummaryResponse(
                subscription.getId(),
                subscription.getUrl(),
                subscription.isActive(),
                subscription.getConsecutiveFailures(),
                extractRegionIds(subscription),
                subscription.getCreatedAt());
    }
}
