package io.github.thena3ik.airalertmonitor.service;

import io.github.thena3ik.airalertmonitor.entity.WebhookSubscription;
import io.github.thena3ik.airalertmonitor.exception.UnauthorizedWebhookAccessException;
import io.github.thena3ik.airalertmonitor.repository.WebhookSubscriptionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WebhookSubscriptionServiceTest {

    @Mock
    private WebhookSubscriptionRepository webhookSubscriptionRepository;

    @InjectMocks
    private WebhookSubscriptionService webhookSubscriptionService;

    @Test
    void deactivatesSubscriptionAfterMaxConsecutiveFailures() {
        WebhookSubscription subscription = new WebhookSubscription("https://example.com", "secret", "token");
        subscription.setConsecutiveFailures(4);
        when(webhookSubscriptionRepository.findById(1L)).thenReturn(Optional.of(subscription));

        webhookSubscriptionService.recordFailure(1L);

        assertThat(subscription.getConsecutiveFailures()).isEqualTo(5);
        assertThat(subscription.isActive()).isFalse();
    }

    @Test
    void resetsFailureCountOnSuccess() {
        WebhookSubscription subscription = new WebhookSubscription("https://example.com", "secret", "token");
        subscription.setConsecutiveFailures(3);

        when(webhookSubscriptionRepository.findById(1L)).thenReturn(Optional.of(subscription));

        webhookSubscriptionService.recordSuccess(1L);

        assertThat(subscription.getConsecutiveFailures()).isEqualTo(0);
    }

    @Test
    void doesNotDeactivate_whenBelowFailureThreshold() {
        WebhookSubscription subscription = new WebhookSubscription("https://example.com", "secret", "token");
        subscription.setConsecutiveFailures(1);

        when(webhookSubscriptionRepository.findById(1L)).thenReturn(Optional.of(subscription));

        webhookSubscriptionService.recordFailure(1L);

        assertThat(subscription.getConsecutiveFailures()).isEqualTo(2);
        assertThat(subscription.isActive()).isTrue();
    }

    @Test
    void doesNotThrow_whenManagementTokenMatches() {
        WebhookSubscription subscription = new WebhookSubscription("https://example.com", "secret", "correct-token");
        when(webhookSubscriptionRepository.findById(1L)).thenReturn(Optional.of(subscription));

        assertThatCode(() -> webhookSubscriptionService.getWebhook(1L, "Bearer correct-token"))
                .doesNotThrowAnyException();
    }

    @Test
    void throwsUnauthorized_whenManagementTokenDoesNotMatch() {
        WebhookSubscription subscription = new WebhookSubscription("https://example.com", "secret", "correct-token");
        when(webhookSubscriptionRepository.findById(1L)).thenReturn(Optional.of(subscription));

        assertThatThrownBy(() -> webhookSubscriptionService.getWebhook(1L, "Bearer wrong-token"))
                .isInstanceOf(UnauthorizedWebhookAccessException.class);
    }
}
