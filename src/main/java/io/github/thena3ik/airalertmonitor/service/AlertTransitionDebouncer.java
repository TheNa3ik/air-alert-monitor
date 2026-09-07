package io.github.thena3ik.airalertmonitor.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AlertTransitionDebouncer {

    private final int requiredConfirmations;
    private final Map<Long, PendingTransition> pending = new ConcurrentHashMap<>();

    private record PendingTransition(boolean candidateActive, int count, Instant firstSeenAt) {}

    public AlertTransitionDebouncer(
            @Value("${air-alert.polling.confirmation-polls:2}") int requiredConfirmations) {
        this.requiredConfirmations = Math.max(1, requiredConfirmations);
    }

    public Optional<Instant> confirm(Long regionId, boolean candidateActive) {
        if (requiredConfirmations <= 1) {
            return Optional.of(Instant.now());
        }

        PendingTransition current = pending.get(regionId);

        if (current == null || current.candidateActive() != candidateActive) {
            pending.put(regionId, new PendingTransition(candidateActive, 1, Instant.now()));
            return Optional.empty();
        }

        int updatedCount = current.count() + 1;
        if (updatedCount >= requiredConfirmations) {
            pending.remove(regionId);
            return Optional.of(current.firstSeenAt());
        }

        pending.put(regionId, new PendingTransition(candidateActive, updatedCount, current.firstSeenAt()));
        return Optional.empty();
    }

    public void clear(Long regionId) {
        pending.remove(regionId);
    }
}
