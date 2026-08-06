package io.github.thena3ik.airalertmonitor.service;

import io.github.thena3ik.airalertmonitor.dto.RegionState;
import io.github.thena3ik.airalertmonitor.dto.UbillingAlertsResponse;
import io.github.thena3ik.airalertmonitor.dto.WebhookEventPayload;
import io.github.thena3ik.airalertmonitor.entity.AlertEvent;
import io.github.thena3ik.airalertmonitor.entity.Region;
import io.github.thena3ik.airalertmonitor.entity.WebhookSubscription;
import io.github.thena3ik.airalertmonitor.notification.WebhookDeliveryClient;
import io.github.thena3ik.airalertmonitor.repository.AlertEventRepository;
import io.github.thena3ik.airalertmonitor.repository.RegionRepository;
import io.github.thena3ik.airalertmonitor.repository.WebhookSubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlertDiffingServiceTest {

    @Mock
    private RegionRepository regionRepository;

    @Mock
    private AlertEventRepository alertEventRepository;

    @Mock
    private WebhookSubscriptionRepository webhookSubscriptionRepository;

    @Mock
    private WebhookDeliveryClient webhookDeliveryClient;

    @InjectMocks
    private AlertDiffingService alertDiffingService;

    @BeforeEach
    void setUp() {
        lenient().when(alertEventRepository.save(any(AlertEvent.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(webhookSubscriptionRepository.findByRegionsContainingAndActiveTrue(any()))
                .thenReturn(List.of());
    }

    private UbillingAlertsResponse responseWith(String regionName, boolean alertNow) {
        return new UbillingAlertsResponse(
                "test-source",
                "2026-08-01 12:00:00",
                Map.of(regionName, new RegionState(alertNow, "2026-08-01 12:00:00")));
    }

    @Test
    void opensNewAlertEvent_whenAlertBecomesActiveAndNoneWasOpen() {
        Region region = new Region(1L, "Донецька область");
        when(regionRepository.findByName("Донецька область")).thenReturn(Optional.of(region));
        when(alertEventRepository.findByRegionAndEndedAtIsNull(region)).thenReturn(Optional.empty());

        alertDiffingService.processPoll(responseWith("Донецька область", true));

        verify(alertEventRepository, times(1)).save(any(AlertEvent.class));
    }

    @Test
    void doesNothing_whenAlertStillActiveAndEventAlreadyOpen() {
        Region region = new Region(1L, "Донецька область");
        AlertEvent existingOpenEvent = new AlertEvent(region, Instant.now(), "test-source");

        when(regionRepository.findByName("Донецька область")).thenReturn(Optional.of(region));
        when(alertEventRepository.findByRegionAndEndedAtIsNull(region)).thenReturn(Optional.of(existingOpenEvent));

        alertDiffingService.processPoll(responseWith("Донецька область", true));

        verify(alertEventRepository, never()).save(any(AlertEvent.class));
    }

    @Test
    void closesOpenAlertEvent_whenAlertBecomesInactive() {
        Region region = new Region(1L, "Донецька область");
        AlertEvent existingOpenEvent = new AlertEvent(region, Instant.now().minusSeconds(600), "test-source");

        when(regionRepository.findByName("Донецька область")).thenReturn(Optional.of(region));
        when(alertEventRepository.findByRegionAndEndedAtIsNull(region)).thenReturn(Optional.of(existingOpenEvent));

        alertDiffingService.processPoll(responseWith("Донецька область", false));

        ArgumentCaptor<AlertEvent> captor = ArgumentCaptor.forClass(AlertEvent.class);
        verify(alertEventRepository, times(1)).save(captor.capture());
        assertThat(captor.getValue().getEndedAt()).isNotNull();
        assertThat(captor.getValue().isOngoing()).isFalse();
    }

    @Test
    void doesNothing_whenAlertInactiveAndNoOpenEvent() {
        Region region = new Region(1L, "Донецька область");
        when(regionRepository.findByName("Донецька область")).thenReturn(Optional.of(region));
        when(alertEventRepository.findByRegionAndEndedAtIsNull(region)).thenReturn(Optional.empty());

        alertDiffingService.processPoll(responseWith("Донецька область", false));

        verify(alertEventRepository, never()).save(any(AlertEvent.class));
    }

    @Test
    void skipsRegion_whenNameNotFoundInDatabase() {
        when(regionRepository.findByName("Unknown Region")).thenReturn(Optional.empty());

        alertDiffingService.processPoll(responseWith("Unknown Region", true));

        verify(alertEventRepository, never()).save(any(AlertEvent.class));
        verify(alertEventRepository, never()).findByRegionAndEndedAtIsNull(any());
    }

    @Test
    void triggersWebhookDelivery_whenAlertStarts() {
        Region region = new Region(1L, "Донецька область");
        WebhookSubscription subscription = new WebhookSubscription("https://example.com/hook", "secret");

        when(regionRepository.findByName("Донецька область")).thenReturn(Optional.of(region));
        when(alertEventRepository.findByRegionAndEndedAtIsNull(region)).thenReturn(Optional.empty());
        when(webhookSubscriptionRepository.findByRegionsContainingAndActiveTrue(region))
                .thenReturn(List.of(subscription));

        alertDiffingService.processPoll(responseWith("Донецька область", true));

        verify(webhookDeliveryClient, times(1)).deliver(eq(subscription), any(WebhookEventPayload.class));
    }

    @Test
    void doesNotTriggerWebhookDelivery_whenNoSubscribersForRegion() {
        Region region = new Region(1L, "Донецька область");
        when(regionRepository.findByName("Донецька область")).thenReturn(Optional.of(region));
        when(alertEventRepository.findByRegionAndEndedAtIsNull(region)).thenReturn(Optional.empty());

        alertDiffingService.processPoll(responseWith("Донецька область", true));

        verify(webhookDeliveryClient, never()).deliver(any(), any());
    }
}