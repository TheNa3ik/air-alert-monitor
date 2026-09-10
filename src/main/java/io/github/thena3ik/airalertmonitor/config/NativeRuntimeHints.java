package io.github.thena3ik.airalertmonitor.config;

import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;

@Configuration
@ImportRuntimeHints(NativeRuntimeHints.Registrar.class)
public class NativeRuntimeHints {

    public static class Registrar implements RuntimeHintsRegistrar {
        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
            hints.reflection().registerType(
                    org.springframework.aot.hint.TypeReference.of("org.flywaydb.core.internal.logging.slf4j.Slf4jLogCreator"),
                    org.springframework.aot.hint.MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS
            );
            hints.resources().registerPattern("org/flywaydb/core/internal/version.txt");

            hints.reflection().registerType(java.sql.Statement[].class);

        }
    }
}