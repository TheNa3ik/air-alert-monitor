package io.github.thena3ik.airalertmonitor.service;

import io.github.thena3ik.airalertmonitor.exception.UnsafeWebhookUrlException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class WebhookUrlValidatorTest {

    @Test
    void rejectsNonHttpsUrlWhenHttpsRequired() {
        var validator = new WebhookUrlValidator(true);
        assertThatThrownBy(() -> validator.validate("http://example.com/hook"))
                .isInstanceOf(UnsafeWebhookUrlException.class);
    }

    @Test
    void rejectsLoopbackAddress() {
        var validator = new WebhookUrlValidator(true);
        assertThatThrownBy(() -> validator.validate("https://localhost/hook"))
                .isInstanceOf(UnsafeWebhookUrlException.class);
    }

    @Test
    void acceptsValidPublicHttpsUrl() {
        var validator = new WebhookUrlValidator(true);
        assertThatCode(() -> validator.validate("https://example.com/hook")).doesNotThrowAnyException();
    }

    @Test
    void acceptsHttpUrlWhenHttpsNotRequired() {
        var validator = new WebhookUrlValidator(false);
        assertThatCode(() -> validator.validate("http://example.com/hook")).doesNotThrowAnyException();
    }

    @Test
    void stillRejectsLoopbackAddressWhenHttpsNotRequired() {
        var validator = new WebhookUrlValidator(false);
        assertThatThrownBy(() -> validator.validate("http://localhost/hook"))
                .isInstanceOf(UnsafeWebhookUrlException.class);
    }
}