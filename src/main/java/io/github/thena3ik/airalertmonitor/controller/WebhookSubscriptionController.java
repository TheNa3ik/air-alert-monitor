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

@Tag(name = "Webhook Subscriptions", description = "api.webhook.tag.desc")
@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
public class WebhookSubscriptionController {

    private final WebhookSubscriptionService webhookSubscriptionService;

    @Operation(summary = "api.webhook.register.summary", description = "api.webhook.register.desc")
    @ApiResponse(responseCode = "201", description = "api.res.webhookCreated")
    @ApiResponse(responseCode = "400", description = "api.err.400.webhookUrl",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                    examples = {
                            @ExampleObject(name = "webhookUrlNotHttps", ref = "#/components/examples/webhookUrlNotHttps"),
                            @ExampleObject(name = "webhookUrlMalformed", ref = "#/components/examples/webhookUrlMalformed")
                    }))
    @ApiResponse(responseCode = "404", description = "api.err.404.noRegions",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(name = "noValidRegionIds", ref = "#/components/examples/noValidRegionIds")))
    @ApiResponse(responseCode = "409", description = "api.err.409.duplicateUrl",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(name = "duplicateWebhookUrl", ref = "#/components/examples/duplicateWebhookUrl")))
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public WebhookSubscriptionResponse createWebhook(@RequestBody CreateWebhookRequest request) {
        return webhookSubscriptionService.createWebhook(request);
    }

    @Operation(summary = "api.webhook.get.summary", description = "api.webhook.get.desc")
    @SecurityRequirement(name = "webhookManagementToken")
    @ApiResponse(responseCode = "200", description = "api.res.webhookSummary")
    @ApiResponse(responseCode = "401", description = "api.err.401.auth",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                    examples = {
                            @ExampleObject(name = "missingAuthHeader", ref = "#/components/examples/missingAuthHeader"),
                            @ExampleObject(name = "invalidManagementToken", ref = "#/components/examples/invalidManagementToken")
                    }))
    @ApiResponse(responseCode = "404", description = "api.err.404.webhookNotFound",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(name = "webhookNotFound", ref = "#/components/examples/webhookNotFound")))
    @GetMapping("/{id}")
    public WebhookSubscriptionSummaryResponse getWebhook(
            @Parameter(description = "api.param.webhookId", required = true) @PathVariable(name = "id") Long subscriptionId,
            @Parameter(hidden = true) @RequestHeader(name = "Authorization", required = false) String authorizationHeader) {
        return webhookSubscriptionService.getWebhook(subscriptionId, authorizationHeader);
    }

    @Operation(summary = "api.webhook.update.summary", description = "api.webhook.update.desc")
    @SecurityRequirement(name = "webhookManagementToken")
    @ApiResponse(responseCode = "200", description = "api.res.webhookUpdated")
    @ApiResponse(responseCode = "401", description = "api.err.401.auth",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                    examples = {
                            @ExampleObject(name = "missingAuthHeader", ref = "#/components/examples/missingAuthHeader"),
                            @ExampleObject(name = "invalidManagementToken", ref = "#/components/examples/invalidManagementToken")
                    }))
    @ApiResponse(responseCode = "404", description = "api.err.404.webhookOrRegionsNotFound",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                    examples = {
                            @ExampleObject(name = "webhookNotFound", ref = "#/components/examples/webhookNotFound"),
                            @ExampleObject(name = "noValidRegionIds", ref = "#/components/examples/noValidRegionIds")
                    }))
    @PutMapping("/{id}/regions")
    public WebhookSubscriptionSummaryResponse updateRegions(
            @Parameter(description = "api.param.webhookId", required = true) @PathVariable(name = "id") Long subscriptionId,
            @Parameter(hidden = true) @RequestHeader(name = "Authorization", required = false) String authorizationHeader,
            @RequestBody UpdateWebhookRegionsRequest request) {
        return webhookSubscriptionService.updateRegions(subscriptionId, request.regionIds(), authorizationHeader);
    }

    @Operation(summary = "api.webhook.reactivate.summary", description = "api.webhook.reactivate.desc")
    @SecurityRequirement(name = "webhookManagementToken")
    @ApiResponse(responseCode = "200", description = "api.res.webhookReactivated")
    @ApiResponse(responseCode = "401", description = "api.err.401.auth",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                    examples = {
                            @ExampleObject(name = "missingAuthHeader", ref = "#/components/examples/missingAuthHeader"),
                            @ExampleObject(name = "invalidManagementToken", ref = "#/components/examples/invalidManagementToken")
                    }))
    @ApiResponse(responseCode = "404", description = "api.err.404.webhookNotFound",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(name = "webhookNotFound", ref = "#/components/examples/webhookNotFound")))
    @PostMapping("/{id}/reactivate")
    public WebhookSubscriptionSummaryResponse reactivateWebhook(
            @Parameter(description = "api.param.webhookId", required = true) @PathVariable(name = "id") Long subscriptionId,
            @Parameter(hidden = true) @RequestHeader(name = "Authorization", required = false) String authorizationHeader) {
        return webhookSubscriptionService.reactivateWebhook(subscriptionId, authorizationHeader);
    }

    @Operation(summary = "api.webhook.delete.summary", description = "api.webhook.delete.desc")
    @SecurityRequirement(name = "webhookManagementToken")
    @ApiResponse(responseCode = "204", description = "api.res.webhookDeleted")
    @ApiResponse(responseCode = "401", description = "api.err.401.auth",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                    examples = {
                            @ExampleObject(name = "missingAuthHeader", ref = "#/components/examples/missingAuthHeader"),
                            @ExampleObject(name = "invalidManagementToken", ref = "#/components/examples/invalidManagementToken")
                    }))
    @ApiResponse(responseCode = "404", description = "api.err.404.webhookNotFound",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(name = "webhookNotFound", ref = "#/components/examples/webhookNotFound")))
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteWebhook(
            @Parameter(description = "api.param.webhookId", required = true) @PathVariable(name = "id") Long subscriptionId,
            @Parameter(hidden = true) @RequestHeader(name = "Authorization", required = false) String authorizationHeader) {
        webhookSubscriptionService.deleteWebhook(subscriptionId, authorizationHeader);
    }
}