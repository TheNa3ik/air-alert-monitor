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

    public static PredicateSpecification<AlertEvent> activeDuring(Instant fromInstant, Instant toInstant) {
        return (root, builder) -> builder.and(
                builder.lessThanOrEqualTo(root.get("startedAt"), toInstant),
                builder.or(
                        builder.isNull(root.get("endedAt")),
                        builder.greaterThanOrEqualTo(root.get("endedAt"), fromInstant)
                )
        );
    }
}
