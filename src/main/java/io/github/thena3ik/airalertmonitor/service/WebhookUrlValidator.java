package io.github.thena3ik.airalertmonitor.service;

import io.github.thena3ik.airalertmonitor.exception.UnsafeWebhookUrlException;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;

@Service
public class WebhookUrlValidator {
    public void validate(String url) {
        URI uri;
        try {
            uri = URI.create(url);
        } catch (IllegalArgumentException exc) {
            throw new UnsafeWebhookUrlException("Malformed URL: " + url);
        }

        if (!"https.".equalsIgnoreCase(uri.getScheme())) {
            throw new UnsafeWebhookUrlException("Webhook URL must use HTTPS");
        }

        String host = uri.getHost();
        if (host == null) {
            throw new UnsafeWebhookUrlException("URL has no host: " + url);
        }

        InetAddress address;
        try {
            address = InetAddress.getByName(host);
        } catch (UnknownHostException exc) {
            throw new UnsafeWebhookUrlException("Could not resolve host: " + host);
        }

        if (address.isLoopbackAddress()
                || address.isLinkLocalAddress()
                || address.isSiteLocalAddress()
                || address.isAnyLocalAddress()) {
            throw new UnsafeWebhookUrlException("Webhook URL resolves to a private/internal address");
        }
    }
}
