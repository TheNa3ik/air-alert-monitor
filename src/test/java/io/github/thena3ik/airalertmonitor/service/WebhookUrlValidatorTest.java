package io.github.thena3ik.airalertmonitor.service;

import io.github.thena3ik.airalertmonitor.exception.UnsafeWebhookUrlException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class WebhookUrlValidatorTest {

    private WebhookUrlValidator validator;

    @BeforeEach
    void setUp() {
        validator = new WebhookUrlValidator();
    }

    @Test
    void rejectsNonHttpsUrl() {
        assertThatThrownBy(() -> validator.validate("http://example.com/hook"))
                .isInstanceOf(UnsafeWebhookUrlException.class);
    }

    @Test
    void rejectsLoopbackAddress() {
        assertThatThrownBy(() -> validator.validate("https://localhost/hook"))
                .isInstanceOf(UnsafeWebhookUrlException.class);
    }

    @Test
    void acceptsValidPublicHttpsUrl() {
        assertThatCode(() -> validator.validate("https://example.com/hook")).doesNotThrowAnyException();
    }
}
