package io.github.thena3ik.airalertmonitor.exception;

public class WebhookDeliveryException extends RuntimeException {
    public WebhookDeliveryException(String message, Throwable cause) {
        super(message, cause);
    }
}
