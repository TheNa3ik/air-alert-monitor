package io.github.thena3ik.airalertmonitor.notification;

import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

@Component
public class WebhookSigner {

    private static final String ALGORITHM = "HmacSHA256";

    public String sign(String payloadJson, String secret) {
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), ALGORITHM));
            byte[] signatureBytes = mac.doFinal(payloadJson.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(signatureBytes);
        } catch (Exception exc) {
            throw new IllegalStateException("Failed to sign webhook payload", exc);
        }
    }
}
