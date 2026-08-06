package io.github.thena3ik.airalertmonitor.controller;

import io.github.thena3ik.airalertmonitor.dto.CreateWebhookRequest;
import io.github.thena3ik.airalertmonitor.dto.WebhookSubscriptionResponse;
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
}
