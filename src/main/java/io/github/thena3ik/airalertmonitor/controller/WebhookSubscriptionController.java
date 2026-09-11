package io.github.thena3ik.airalertmonitor.controller;

import io.github.thena3ik.airalertmonitor.dto.common.ErrorResponse;
import io.github.thena3ik.airalertmonitor.dto.webhook.CreateWebhookRequest;
import io.github.thena3ik.airalertmonitor.dto.webhook.UpdateWebhookRegionsRequest;
import io.github.thena3ik.airalertmonitor.dto.webhook.WebhookSubscriptionResponse;
import io.github.thena3ik.airalertmonitor.dto.webhook.WebhookSubscriptionSummaryResponse;
import io.github.thena3ik.airalertmonitor.service.WebhookSubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Webhook Subscriptions", description = "Register and manage webhook subscriptions that receive alert start/end notifications")
@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
public class WebhookSubscriptionController {

    private final WebhookSubscriptionService webhookSubscriptionService;

    @Operation(
            summary = "Register a webhook subscription",
            description = "Creates a new webhook subscription for the given regions. The provided URL MUST be HTTPS and "
                    + "must resolve to a public, non-internal host. "
                    + "The response includes a `secret` (used to verify the HMAC signature on delivered payloads) and a `managementToken` "
                    + "(used as a bearer token to manage this subscription later). Both values are shown only once."
    )
    @ApiResponse(responseCode = "201", description = "Subscription created")
    @ApiResponse(responseCode = "400", description = "The webhook URL is malformed, not HTTPS, has no resolvable host, "
            + "or resolves to a private/internal address",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                    examples = {
                            @ExampleObject(name = "webhookUrlNotHttps", ref = "#/components/examples/webhookUrlNotHttps"),
                            @ExampleObject(name = "webhookUrlMalformed", ref = "#/components/examples/webhookUrlMalformed")
                    }))
    @ApiResponse(responseCode = "404", description = "None of the provided region ids exist",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(name = "noValidRegionIds", ref = "#/components/examples/noValidRegionIds")))
    @ApiResponse(responseCode = "409", description = "An active subscription for this URL already exists",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(name = "duplicateWebhookUrl", ref = "#/components/examples/duplicateWebhookUrl")))
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public WebhookSubscriptionResponse createWebhook(@RequestBody CreateWebhookRequest request) {
        return webhookSubscriptionService.createWebhook(request);
    }

    @Operation(
            summary = "Get a webhook subscription",
            description = "Returns a summary of the subscription (without the secret). Requires the management token."
    )
    @SecurityRequirement(name = "webhookManagementToken")
    @ApiResponse(responseCode = "200", description = "Subscription summary")
    @ApiResponse(responseCode = "401", description = "Authorization header is missing/malformed, or the management token is invalid",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                    examples = {
                            @ExampleObject(name = "missingAuthHeader", ref = "#/components/examples/missingAuthHeader"),
                            @ExampleObject(name = "invalidManagementToken", ref = "#/components/examples/invalidManagementToken")
                    }))
    @ApiResponse(responseCode = "404", description = "Subscription not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(name = "webhookNotFound", ref = "#/components/examples/webhookNotFound")))
    @GetMapping("/{id}")
    public WebhookSubscriptionSummaryResponse getWebhook(
            @Parameter(description = "Webhook subscription id", required = true)
            @PathVariable(name = "id") Long subscriptionId,
            @Parameter(hidden = true)
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader) {
        return webhookSubscriptionService.getWebhook(subscriptionId, authorizationHeader);
    }

    @Operation(
            summary = "Update the regions a webhook is subscribed to",
            description = "Replaces the full set of regions for this subscription. Requires the management token."
    )
    @SecurityRequirement(name = "webhookManagementToken")
    @ApiResponse(responseCode = "200", description = "Updated subscription summary")
    @ApiResponse(responseCode = "401", description = "Authorization header is missing/malformed, or the management token is invalid",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                    examples = {
                            @ExampleObject(name = "missingAuthHeader", ref = "#/components/examples/missingAuthHeader"),
                            @ExampleObject(name = "invalidManagementToken", ref = "#/components/examples/invalidManagementToken")
                    }))
    @ApiResponse(responseCode = "404", description = "Subscription not found, or none of the provided region ids exist",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                    examples = {
                            @ExampleObject(name = "webhookNotFound", ref = "#/components/examples/webhookNotFound"),
                            @ExampleObject(name = "noValidRegionIds", ref = "#/components/examples/noValidRegionIds")
                    }))
    @PutMapping("/{id}/regions")
    public WebhookSubscriptionSummaryResponse updateRegions(
            @Parameter(description = "Webhook subscription id", required = true)
            @PathVariable(name = "id") Long subscriptionId,
            @Parameter(hidden = true)
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader,
            @RequestBody UpdateWebhookRegionsRequest request) {
        return webhookSubscriptionService.updateRegions(subscriptionId, request.regionIds(), authorizationHeader);
    }

    @Operation(
            summary = "Reactivate a webhook subscription",
            description = "Re-enables a subscription that was auto-disabled after repeated delivery failures. Requires the management token."
    )
    @SecurityRequirement(name = "webhookManagementToken")
    @ApiResponse(responseCode = "200", description = "Reactivated subscription summary")
    @ApiResponse(responseCode = "401", description = "Authorization header is missing/malformed, or the management token is invalid",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                    examples = {
                            @ExampleObject(name = "missingAuthHeader", ref = "#/components/examples/missingAuthHeader"),
                            @ExampleObject(name = "invalidManagementToken", ref = "#/components/examples/invalidManagementToken")
                    }))
    @ApiResponse(responseCode = "404", description = "Subscription not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(name = "webhookNotFound", ref = "#/components/examples/webhookNotFound")))
    @PostMapping("/{id}/reactivate")
    public WebhookSubscriptionSummaryResponse reactivateWebhook(
            @Parameter(description = "Webhook subscription id", required = true)
            @PathVariable(name = "id") Long subscriptionId,
            @Parameter(hidden = true)
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader) {
        return webhookSubscriptionService.reactivateWebhook(subscriptionId, authorizationHeader);
    }

    @Operation(
            summary = "Delete a webhook subscription",
            description = "Permanently removes the subscription. Requires the management token."
    )
    @SecurityRequirement(name = "webhookManagementToken")
    @ApiResponse(responseCode = "204", description = "Subscription deleted")
    @ApiResponse(responseCode = "401", description = "Authorization header is missing/malformed, or the management token is invalid",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                    examples = {
                            @ExampleObject(name = "missingAuthHeader", ref = "#/components/examples/missingAuthHeader"),
                            @ExampleObject(name = "invalidManagementToken", ref = "#/components/examples/invalidManagementToken")
                    }))
    @ApiResponse(responseCode = "404", description = "Subscription not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(name = "webhookNotFound", ref = "#/components/examples/webhookNotFound")))
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteWebhook(
            @Parameter(description = "Webhook subscription id", required = true)
            @PathVariable(name = "id") Long subscriptionId,
            @Parameter(hidden = true)
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader) {
        webhookSubscriptionService.deleteWebhook(subscriptionId, authorizationHeader);
    }
}