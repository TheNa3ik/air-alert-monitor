package io.github.thena3ik.airalertmonitor.controller;

import io.github.thena3ik.airalertmonitor.dto.webhook.CreateWebhookRequest;
import io.github.thena3ik.airalertmonitor.dto.webhook.UpdateWebhookRegionsRequest;
import io.github.thena3ik.airalertmonitor.dto.webhook.WebhookSubscriptionResponse;
import io.github.thena3ik.airalertmonitor.dto.webhook.WebhookSubscriptionSummaryResponse;
import io.github.thena3ik.airalertmonitor.service.WebhookSubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
public class WebhookSubscriptionController {

    private final WebhookSubscriptionService webhookSubscriptionService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public WebhookSubscriptionResponse createWebhook(@RequestBody CreateWebhookRequest request) {
        return webhookSubscriptionService.createWebhook(request);
    }

    @GetMapping("/{id}")
    public WebhookSubscriptionSummaryResponse getWebhook(
            @PathVariable(name = "id") Long subscriptionId,
            @RequestHeader(name = "Authorization") String authorizationHeader) {
        return webhookSubscriptionService.getWebhook(subscriptionId, authorizationHeader);
    }

    @PutMapping("/{id}/regions")
    public WebhookSubscriptionSummaryResponse updateRegions(
            @PathVariable(name = "id") Long subscriptionId,
            @RequestHeader(name = "Authorization") String authorizationHeader,
            @RequestBody UpdateWebhookRegionsRequest request) {
        return webhookSubscriptionService.updateRegions(subscriptionId, request.regionIds(), authorizationHeader);
    }

    @PostMapping("/{id}/reactivate")
    public WebhookSubscriptionSummaryResponse reactivateWebhook(
            @PathVariable(name = "id") Long subscriptionId,
            @RequestHeader(name = "Authorization") String authorizationHeader) {
        return webhookSubscriptionService.reactivateWebhook(subscriptionId, authorizationHeader);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteWebhook(
            @PathVariable(name = "id") Long subscriptionId,
            @RequestHeader(name = "Authorization") String authorizationHeader) {
        webhookSubscriptionService.deleteWebhook(subscriptionId, authorizationHeader);
    }
}