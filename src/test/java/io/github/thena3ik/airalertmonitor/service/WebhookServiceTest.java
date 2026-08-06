package io.github.thena3ik.airalertmonitor.service;

import io.github.thena3ik.airalertmonitor.entity.WebhookSubscription;
import io.github.thena3ik.airalertmonitor.repository.WebhookSubscriptionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WebhookServiceTest {

    @Mock
    private WebhookSubscriptionRepository webhookSubscriptionRepository;

    @InjectMocks
    private WebhookService webhookService;

    @Test
    void deactivatesSubscriptionAfterMaxConsecutiveFailures() {
        WebhookSubscription subscription = new WebhookSubscription("https://example.com", "secret");
        subscription.setConsecutiveFailures(4);
        when(webhookSubscriptionRepository.findById(1L)).thenReturn(Optional.of(subscription));

        webhookService.recordFailure(1L);

        assertThat(subscription.getConsecutiveFailures()).isEqualTo(5);
        assertThat(subscription.isActive()).isFalse();
    }

    @Test
    void resetsFailureCountOnSuccess() {
        WebhookSubscription subscription = new WebhookSubscription("https://example.com", "secret");
        subscription.setConsecutiveFailures(3);

        when(webhookSubscriptionRepository.findById(1L)).thenReturn(Optional.of(subscription));

        webhookService.recordSuccess(1L);

        assertThat(subscription.getConsecutiveFailures()).isEqualTo(0);
    }

    @Test
    void doesNotDeactivate_whenBelowFailureThreshold() {
        WebhookSubscription subscription = new WebhookSubscription("https://example.com", "secret");
        subscription.setConsecutiveFailures(1);

        when(webhookSubscriptionRepository.findById(1L)).thenReturn(Optional.of(subscription));

        webhookService.recordFailure(1L);

        assertThat(subscription.getConsecutiveFailures()).isEqualTo(2);
        assertThat(subscription.isActive()).isTrue();
    }
}
