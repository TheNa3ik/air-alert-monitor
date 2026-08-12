package io.github.thena3ik.airalertmonitor.specification;

import io.github.thena3ik.airalertmonitor.entity.Region;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.PredicateSpecification;

import java.util.List;

public class RegionSpecifications {

    private RegionSpecifications() {}

    public static PredicateSpecification<Region> hasIdIn(List<Long> ids) {
        return (root, builder) -> root.get("id").in(ids);
    }

    public static PredicateSpecification<Region> hasNameIn(List<String> names) {
        return (root, builder) -> {
            Predicate matchesUkrainianName = root.get("name").in(names);
            Predicate matchesEnglishName = root.get("nameEn").in(names);
            return builder.or(matchesUkrainianName, matchesEnglishName);
        };
    }
}
