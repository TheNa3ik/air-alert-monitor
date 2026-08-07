package io.github.thena3ik.airalertmonitor.exception;

public class WebhookNotFoundException extends RuntimeException {
    public WebhookNotFoundException(String message) {
        super(message);
    }
}
