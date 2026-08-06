package io.github.thena3ik.airalertmonitor.notification;

import io.github.thena3ik.airalertmonitor.dto.WebhookEventPayload;
import io.github.thena3ik.airalertmonitor.entity.WebhookSubscription;
import io.github.thena3ik.airalertmonitor.exception.WebhookDeliveryException;
import io.github.thena3ik.airalertmonitor.service.WebhookUrlValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebhookDeliveryClient {

    private final RestClient.Builder restClientBuilder;
    private final WebhookSigner webhookSigner;
    private final WebhookUrlValidator webhookUrlValidator;
    private final JsonMapper jsonMapper;

    @Async
    public void deliver(WebhookSubscription subscription, WebhookEventPayload payload) {
        try {
            webhookUrlValidator.validate(subscription.getUrl());

            String payloadJson = jsonMapper.writeValueAsString(payload);
            String signature = webhookSigner.sign(payloadJson, subscription.getSecret());

            RestClient client = restClientBuilder
                    .requestFactory(timeoutRequestFactory())
                    .build();

            client.post()
                    .uri(subscription.getUrl())
                    .header("Content-Type", "application/json")
                    .header("X-Signature", signature)
                    .body(payloadJson)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Webhook delivered successfully to subscription {}", subscription.getId());

        } catch (Exception exc) {
            log.warn("Webhook delivery failed for subscription {}: {}", subscription.getId(), exc.getMessage());
            throw new WebhookDeliveryException("Failed to deliver webhook to subscription " + subscription.getId(), exc);
        }
    }

    private ClientHttpRequestFactory timeoutRequestFactory() {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) Duration.ofSeconds(5).toMillis());
        factory.setReadTimeout((int) Duration.ofSeconds(5).toMillis());
        return factory;
    }
}
