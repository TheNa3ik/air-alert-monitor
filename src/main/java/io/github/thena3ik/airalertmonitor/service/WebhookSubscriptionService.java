package io.github.thena3ik.airalertmonitor.service;

import io.github.thena3ik.airalertmonitor.dto.webhook.CreateWebhookRequest;
import io.github.thena3ik.airalertmonitor.dto.webhook.WebhookSubscriptionResponse;
import io.github.thena3ik.airalertmonitor.dto.webhook.WebhookSubscriptionSummaryResponse;
import io.github.thena3ik.airalertmonitor.entity.Region;
import io.github.thena3ik.airalertmonitor.entity.WebhookSubscription;
import io.github.thena3ik.airalertmonitor.exception.DuplicateWebhookSubscriptionException;
import io.github.thena3ik.airalertmonitor.exception.RegionNotFoundException;
import io.github.thena3ik.airalertmonitor.exception.UnauthorizedWebhookAccessException;
import io.github.thena3ik.airalertmonitor.exception.WebhookNotFoundException;
import io.github.thena3ik.airalertmonitor.repository.RegionRepository;
import io.github.thena3ik.airalertmonitor.repository.WebhookSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
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
        webhookSubscriptionRepository.findById(subscriptionId).ifPresent(subscription -> {
            subscription.setConsecutiveFailures(0);
            webhookSubscriptionRepository.save(subscription);
        });
    }

    @Transactional
    public void recordFailure(Long subscriptionId) {
        webhookSubscriptionRepository.findById(subscriptionId).ifPresent(subscription -> {
            subscription.setConsecutiveFailures(subscription.getConsecutiveFailures() + 1);

            if (subscription.getConsecutiveFailures() >= MAX_CONSECUTIVE_FAILURES) {
                subscription.setActive(false);
                log.warn("Subscription {} deactivated after {} consecutive failures", subscriptionId, MAX_CONSECUTIVE_FAILURES);
            }

            webhookSubscriptionRepository.save(subscription);
        });
    }

    public WebhookSubscriptionSummaryResponse getWebhook(Long subscriptionId, String authorizationHeader) {
        WebhookSubscription subscription = getSubscriptionOrThrow(subscriptionId);

        verifyManagementToken(subscription, extractBearerToken(authorizationHeader));

        return toSubscriptionSummaryResponse(subscription);
    }

    public WebhookSubscriptionSummaryResponse updateRegions(Long subscriptionId,
                                                            List<Long> regionIds,
                                                            String authorizationHeader) {
        WebhookSubscription subscription = getSubscriptionOrThrow(subscriptionId);

        verifyManagementToken(subscription, extractBearerToken(authorizationHeader));

        List<Region> regions = regionRepository.findAllById(regionIds);
        if (regions.isEmpty()) {
            throw new RegionNotFoundException("No valid regions found for provided regionIds");
        }

        subscription.setRegions(new HashSet<>(regions));
        webhookSubscriptionRepository.save(subscription);

        return toSubscriptionSummaryResponse(subscription);
    }

    public WebhookSubscriptionSummaryResponse reactivateWebhook(Long subscriptionId, String authorizationHeader) {
        WebhookSubscription subscription = getSubscriptionOrThrow(subscriptionId);

        verifyManagementToken(subscription, extractBearerToken(authorizationHeader));

        subscription.setActive(true);
        subscription.setConsecutiveFailures(0);
        webhookSubscriptionRepository.save(subscription);

        return toSubscriptionSummaryResponse(subscription);
    }

    public void deleteWebhook(Long subscriptionId, String authorizationHeader) {
        WebhookSubscription subscription = getSubscriptionOrThrow(subscriptionId);

        verifyManagementToken(subscription, extractBearerToken(authorizationHeader));

        webhookSubscriptionRepository.deleteById(subscriptionId);
    }

    public WebhookSubscriptionResponse createWebhook(CreateWebhookRequest request) {
        webhookUrlValidator.validate(request.url());

        if (webhookSubscriptionRepository.findByUrlAndActiveTrue(request.url()).isPresent()) {
            throw new DuplicateWebhookSubscriptionException(
                    "An active webhook subscription already exists for this URL: " + request.url());
        }

        List<Region> regions = regionRepository.findAllById(request.regionIds());
        if (regions.isEmpty()) {
            throw new RegionNotFoundException("No valid regions found for provided regionIds");
        }

        String secret = generateSecureToken();
        String managementToken = generateSecureToken();
        WebhookSubscription subscription = new WebhookSubscription(request.url(), secret, managementToken);
        subscription.setRegions(new HashSet<>(regions));

        webhookSubscriptionRepository.save(subscription);

        return toSubscriptionResponse(subscription);
    }

    private WebhookSubscription getSubscriptionOrThrow(Long subscriptionId) {
        return webhookSubscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new WebhookNotFoundException("Webhook not found with id: " + subscriptionId));
    }

    private String generateSecureToken() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new UnauthorizedWebhookAccessException("Missing or malformed Authorization header");
        }
        return authorizationHeader.substring("Bearer ".length()).trim();
    }

    private void verifyManagementToken(WebhookSubscription subscription, String providedToken) {
        boolean matches = MessageDigest.isEqual(
                subscription.getManagementToken().getBytes(StandardCharsets.UTF_8),
                providedToken.getBytes(StandardCharsets.UTF_8));
        if (!matches) {
            throw new UnauthorizedWebhookAccessException("Invalid management token");
        }
    }

    private List<Long> extractRegionIds(WebhookSubscription subscription) {
        return subscription.getRegions().stream().map(Region::getId).toList();
    }

    private WebhookSubscriptionResponse toSubscriptionResponse(WebhookSubscription subscription) {
        return new WebhookSubscriptionResponse(
                subscription.getId(),
                subscription.getUrl(),
                subscription.getSecret(),
                subscription.getManagementToken(),
                subscription.isActive(),
                extractRegionIds(subscription),
                subscription.getCreatedAt());
    }

    private WebhookSubscriptionSummaryResponse toSubscriptionSummaryResponse(WebhookSubscription subscription) {
        return new WebhookSubscriptionSummaryResponse(
                subscription.getId(),
                subscription.getUrl(),
                subscription.isActive(),
                subscription.getConsecutiveFailures(),
                extractRegionIds(subscription),
                subscription.getCreatedAt());
    }
}