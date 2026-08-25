package io.github.thena3ik.airalertmonitor.repository;

import io.github.thena3ik.airalertmonitor.entity.Region;
import io.github.thena3ik.airalertmonitor.entity.WebhookSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WebhookSubscriptionRepository extends JpaRepository<WebhookSubscription, Long> {
    List<WebhookSubscription> findByRegionsContainingAndActiveTrue(Region region);
    Optional<WebhookSubscription> findByUrlAndActiveTrue(String url);
}
