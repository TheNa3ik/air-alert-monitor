package io.github.thena3ik.airalertmonitor.specification;

import io.github.thena3ik.airalertmonitor.entity.AlertEvent;
import io.github.thena3ik.airalertmonitor.entity.Region;
import org.springframework.data.jpa.domain.PredicateSpecification;

import java.time.Instant;
import java.util.List;

public class AlertEventSpecifications {

    private AlertEventSpecifications() {}

    public static PredicateSpecification<AlertEvent> hasRegionIn(List<Region> regions) {
        return (root, builder) -> root.get("region").in(regions);
    }

    public static PredicateSpecification<AlertEvent> startedBetween(Instant fromInstant, Instant toInstant) {
        return (root, builder) -> builder.between(root.get("startedAt"), fromInstant, toInstant);
    }
}
