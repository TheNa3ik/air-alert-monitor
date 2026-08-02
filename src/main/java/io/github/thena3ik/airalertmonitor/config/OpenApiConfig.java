package io.github.thena3ik.airalertmonitor.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI airAlertMonitorOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Air Alert Monitor API")
                        .description("Tracks Ukrainian air raid alert history and stats, built on top of the Ubilling aerial alerts feed.")
                        .version("v1"));
    }
}
