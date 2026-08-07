package io.github.thena3ik.airalertmonitor.exception;

public class UnauthorizedWebhookAccessException extends RuntimeException {
    public UnauthorizedWebhookAccessException(String message) {
        super(message);
    }
}
