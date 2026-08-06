package io.github.thena3ik.airalertmonitor.notification;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class WebhookSignerTest {

    @Test
    void signsPayloadConsistently() {
        WebhookSigner signer = new WebhookSigner();
        String signature1 = signer.sign("{\"test\":true}", "my-secret");
        String signature2 = signer.sign("{\"test\":true}", "my-secret");
        assertThat(signature1).isEqualTo(signature2);
    }

    @Test
    void differentSecretsProduceDifferentSignatures() {
        WebhookSigner signer = new WebhookSigner();
        String sig1 = signer.sign("{\"test\":true}", "secret-a");
        String sig2 = signer.sign("{\"test\":true}", "secret-b");
        assertThat(sig1).isNotEqualTo(sig2);
    }
}
