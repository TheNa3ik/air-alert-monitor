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
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
public class WebhookSubscriptionController {

    private final WebhookSubscriptionService webhookSubscriptionService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public WebhookSubscriptionResponse createWebhook (@RequestBody CreateWebhookRequest request) {
        return webhookSubscriptionService.createWebhook(request);
    }

    @GetMapping("/{id}")
    public WebhookSubscriptionSummaryResponse getWebhook(@PathVariable Long id,
                                                         @RequestHeader("Authorization") String authorization) {
        return webhookSubscriptionService.getWebhook(id, authorization);
    }

    @PutMapping("/{id}/regions")
    public WebhookSubscriptionSummaryResponse updateRegions(@PathVariable Long id,
                                                            @RequestHeader("Authorization") String authorization,
                                                            @RequestBody UpdateWebhookRegionsRequest request) {
        return webhookSubscriptionService.updateRegions(id, request.regionIds(), authorization);
    }

    @PostMapping("/{id}/reactivate")
    public WebhookSubscriptionSummaryResponse reactivateWebhook(@PathVariable Long id,
                                                                @RequestHeader("Authorization") String authorization) {
        return webhookSubscriptionService.reactivateWebhook(id, authorization);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteWebhook(@PathVariable Long id,
                              @RequestHeader("Authorization") String authorization) {
        webhookSubscriptionService.deleteWebhook(id, authorization);
    }
}
