package io.github.thena3ik.airalertmonitor.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

@Component
public class SchemaAndWebhookI18nCustomizer implements GlobalOpenApiCustomizer {

    private static final Pattern WRAPPED_KEY = Pattern.compile("^\\$\\{(api\\..+)}$");

    private final MessageSource messageSource;

    public SchemaAndWebhookI18nCustomizer(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    public void customise(OpenAPI openApi) {
        Locale locale = LocaleContextHolder.getLocale();

        if (openApi.getInfo() != null) {
            openApi.getInfo().setDescription(resolve(openApi.getInfo().getDescription(), locale));
        }

        if (openApi.getComponents() != null && openApi.getComponents().getSecuritySchemes() != null) {
            openApi.getComponents().getSecuritySchemes().values()
                    .forEach(scheme -> scheme.setDescription(resolve(scheme.getDescription(), locale)));
        }

        if (openApi.getComponents() != null && openApi.getComponents().getSchemas() != null) {
            Set<Schema<?>> visited = new HashSet<>();
            openApi.getComponents().getSchemas().values()
                    .forEach(schema -> localizeSchema(schema, locale, visited));
        }

        if (openApi.getWebhooks() != null) {
            openApi.getWebhooks().values().forEach(pathItem -> localizeWebhookPathItem(pathItem, locale));
        }
    }

    private void localizeSchema(Schema<?> schema, Locale locale, Set<Schema<?>> visited) {
        if (schema == null || !visited.add(schema)) {
            return;
        }

        schema.setDescription(resolve(schema.getDescription(), locale));

        if (schema.getProperties() != null) {
            for (Object value : schema.getProperties().values()) {
                if (value instanceof Schema<?> propertySchema) {
                    localizeSchema(propertySchema, locale, visited);
                }
            }
        }

        if (schema.getItems() != null) {
            localizeSchema(schema.getItems(), locale, visited);
        }
    }

    private void localizeWebhookPathItem(PathItem pathItem, Locale locale) {
        pathItem.setSummary(resolve(pathItem.getSummary(), locale));
        pathItem.setDescription(resolve(pathItem.getDescription(), locale));

        for (Operation operation : pathItem.readOperationsMap().values()) {
            operation.setSummary(resolve(operation.getSummary(), locale));
            operation.setDescription(resolve(operation.getDescription(), locale));

            RequestBody requestBody = operation.getRequestBody();
            if (requestBody != null) {
                requestBody.setDescription(resolve(requestBody.getDescription(), locale));
            }

            if (operation.getResponses() != null) {
                for (ApiResponse response : operation.getResponses().values()) {
                    response.setDescription(resolve(response.getDescription(), locale));
                }
            }
        }
    }

    private String resolve(String value, Locale locale) {
        if (value == null) {
            return null;
        }

        var wrappedMatcher = WRAPPED_KEY.matcher(value);
        String key = wrappedMatcher.matches() ? wrappedMatcher.group(1)
                : value.startsWith("api.") ? value
                : null;

        if (key == null) {
            return value;
        }

        return messageSource.getMessage(key, null, value, locale);
    }
}