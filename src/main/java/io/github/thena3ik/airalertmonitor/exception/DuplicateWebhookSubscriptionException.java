package io.github.thena3ik.airalertmonitor.exception;

public class DuplicateWebhookSubscriptionException extends RuntimeException {
    public DuplicateWebhookSubscriptionException(String message) {
        super(message);
    }
}
