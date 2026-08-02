package io.github.thena3ik.airalertmonitor.repository;

import io.github.thena3ik.airalertmonitor.entity.AlertEvent;
import io.github.thena3ik.airalertmonitor.entity.Region;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface AlertEventRepository extends JpaRepository<AlertEvent, Long> {
    Optional<AlertEvent> findByRegionAndEndedAtIsNull(Region region);
    Page<AlertEvent> findByRegion(Region region, Pageable pageable);
    List<AlertEvent> findByRegionOrderByStartedAtDesc(Region region);
    List<AlertEvent> findByRegionAndStartedAtBetween(Region region, Instant from, Instant to);
}
