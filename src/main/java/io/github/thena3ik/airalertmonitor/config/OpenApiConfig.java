package io.github.thena3ik.airalertmonitor.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.thena3ik.airalertmonitor.dto.webhook.WebhookEventPayload;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.core.converter.ResolvedSchema;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Configuration
public class OpenApiConfig {

    private static final ObjectMapper JSON = new ObjectMapper();

    @Bean
    public OpenAPI airAlertMonitorOpenApi() {
        ResolvedSchema resolvedSchema = ModelConverters.getInstance()
                .resolveAsResolvedSchema(new AnnotatedType(WebhookEventPayload.class));

        Components components = new Components()
                .addSecuritySchemes("webhookManagementToken", new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .description("api.security.webhookManagementToken.desc"))
                .addSecuritySchemes("webhookSignature", new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.HEADER)
                        .name("X-Signature")
                        .description("api.security.webhookSignature.desc"))
                .addExamples("regionNotFound", errorExample("""
                        {"status":404,"message":"Region not found with regionId: 99","timestamp":"2026-09-11T07:03:07.564Z"}"""))
                .addExamples("invalidPeriod", errorExample("""
                        {"status":400,"message":"Unknown period: fortnight","timestamp":"2026-09-11T07:03:07.564Z"}"""))
                .addExamples("webhookNotFound", errorExample("""
                        {"status":404,"message":"Webhook not found with id: 42","timestamp":"2026-09-11T07:03:07.564Z"}"""))
                .addExamples("invalidManagementToken", errorExample("""
                        {"status":401,"message":"Invalid management token","timestamp":"2026-09-11T07:03:07.564Z"}"""))
                .addExamples("noValidRegionIds", errorExample("""
                        {"status":404,"message":"No valid regions found for provided regionIds","timestamp":"2026-09-11T07:03:07.564Z"}"""))
                .addExamples("webhookUrlNotHttps", errorExample("""
                        {"status":400,"message":"Webhook URL must use HTTPS","timestamp":"2026-09-11T07:03:07.564Z"}"""))
                .addExamples("duplicateWebhookUrl", errorExample("""
                        {"status":409,"message":"An active webhook subscription already exists for this URL: https://example.com/hooks","timestamp":"2026-09-11T07:03:07.564Z"}"""))
                .addExamples("invalidTimezone", errorExample("""
                        {"status":400,"message":"Invalid timezone: Fooland/Nowhere","timestamp":"2026-09-11T07:03:07.564Z"}"""))
                .addExamples("invalidFilter", errorExample("""
                        {"status":400,"message":"Cannot filter by both 'ids' and 'names' simultaneously.","timestamp":"2026-09-11T07:03:07.564Z"}"""))
                .addExamples("missingAuthHeader", errorExample("""
                        {"status":401,"message":"Missing or malformed Authorization header","timestamp":"2026-09-11T07:03:07.564Z"}"""))
                .addExamples("webhookUrlMalformed", errorExample("""
                        {"status":400,"message":"Malformed URL: not-a-url","timestamp":"2026-09-11T07:03:07.564Z"}"""))
                .addExamples("invalidDateRange", errorExample("""
                        {"status":400,"message":"'from' must not be after 'to'","timestamp":"2026-09-11T07:03:07.564Z"}"""));

        components.addSchemas(resolvedSchema.schema.getName(), resolvedSchema.schema);
        resolvedSchema.referencedSchemas.forEach(components::addSchemas);

        PathItem alertEventCallback = new PathItem()
                .post(new Operation()
                        .summary("api.webhook.callback.summary")
                        .description("api.webhook.callback.desc")
                        .security(List.of(new SecurityRequirement().addList("webhookSignature")))
                        .requestBody(new RequestBody()
                                .required(true)
                                .content(new Content().addMediaType("application/json",
                                        new MediaType().schema(new Schema<>().$ref("#/components/schemas/WebhookEventPayload")))))
                        .responses(new ApiResponses()
                                .addApiResponse("200", new ApiResponse().description("api.webhook.callback.res.200"))
                                .addApiResponse("5XX", new ApiResponse().description("api.webhook.callback.res.5xx"))
                                .addApiResponse("default", new ApiResponse().description("api.webhook.callback.res.default"))));

        return new OpenAPI()
                .info(new Info()
                        .title("Air Alert Monitor API")
                        .description("api.info.desc")
                        .version("v1"))
                .components(components)
                .webhooks(Map.of("alertEvent", alertEventCallback));
    }

    private static Example errorExample(String json) {
        try {
            return new Example().value(JSON.readValue(json, Object.class));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Invalid example JSON literal in OpenApiConfig", e);
        }
    }
}