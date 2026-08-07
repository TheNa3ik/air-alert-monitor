package io.github.thena3ik.airalertmonitor.controller;

import io.github.thena3ik.airalertmonitor.dto.CreateWebhookRequest;
import io.github.thena3ik.airalertmonitor.dto.UpdateWebhookRegionsRequest;
import io.github.thena3ik.airalertmonitor.dto.WebhookSubscriptionResponse;
import io.github.thena3ik.airalertmonitor.dto.WebhookSubscriptionSummaryResponse;
import io.github.thena3ik.airalertmonitor.service.WebhookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
public class WebhookController {

    private final WebhookService webhookService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public WebhookSubscriptionResponse createWebhook (@RequestBody CreateWebhookRequest request) {
        return webhookService.createWebhook(request);
    }

    @GetMapping("/{id}")
    public WebhookSubscriptionSummaryResponse getWebhook(@PathVariable Long id) {
        return webhookService.getWebhook(id);
    }

    @PutMapping("/{id}/regions")
    public WebhookSubscriptionSummaryResponse updateRegions(@PathVariable Long id,
                                                            @RequestBody UpdateWebhookRegionsRequest request) {
        return webhookService.updateRegions(id, request.regionIds());
    }

    @PostMapping("/{id}/reactivate")
    public WebhookSubscriptionSummaryResponse reactivateWebhook(@PathVariable Long id) {
        return webhookService.reactivateWebhook(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteWebhook(@PathVariable Long id) {
        webhookService.deleteWebhook(id);
    }
}
