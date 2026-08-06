package io.github.thena3ik.airalertmonitor.service;

import io.github.thena3ik.airalertmonitor.dto.CreateWebhookRequest;
import io.github.thena3ik.airalertmonitor.dto.WebhookSubscriptionResponse;
import io.github.thena3ik.airalertmonitor.entity.Region;
import io.github.thena3ik.airalertmonitor.entity.WebhookSubscription;
import io.github.thena3ik.airalertmonitor.exception.RegionNotFoundException;
import io.github.thena3ik.airalertmonitor.repository.RegionRepository;
import io.github.thena3ik.airalertmonitor.repository.WebhookSubscriptionRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;

@Service
@AllArgsConstructor
public class WebhookService {

    private final WebhookUrlValidator webhookUrlValidator;
    private final RegionRepository regionRepository;
    private final WebhookSubscriptionRepository webhookSubscriptionRepository;

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

    private WebhookSubscriptionResponse toResponse(WebhookSubscription subscription) {
        List<Long> regionIds = subscription.getRegions().stream().map(Region::getId).toList();
        return new WebhookSubscriptionResponse(
                subscription.getId(),
                subscription.getUrl(),
                subscription.getSecret(),
                subscription.isActive(),
                regionIds,
                subscription.getCreatedAt());
    }
}
