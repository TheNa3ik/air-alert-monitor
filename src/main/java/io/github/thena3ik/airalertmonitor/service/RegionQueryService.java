package io.github.thena3ik.airalertmonitor.service;

import io.github.thena3ik.airalertmonitor.dto.common.PageResponse;
import io.github.thena3ik.airalertmonitor.dto.region.AlertEventResponse;
import io.github.thena3ik.airalertmonitor.dto.region.RegionAlertStatsResponse;
import io.github.thena3ik.airalertmonitor.dto.region.RegionStatusResponse;
import io.github.thena3ik.airalertmonitor.entity.AlertEvent;
import io.github.thena3ik.airalertmonitor.entity.Region;
import io.github.thena3ik.airalertmonitor.exception.InvalidDateRangeException;
import io.github.thena3ik.airalertmonitor.exception.InvalidPeriodException;
import io.github.thena3ik.airalertmonitor.exception.InvalidTimezoneException;
import io.github.thena3ik.airalertmonitor.exception.RegionNotFoundException;
import io.github.thena3ik.airalertmonitor.repository.AlertEventRepository;
import io.github.thena3ik.airalertmonitor.repository.RegionRepository;
import io.github.thena3ik.airalertmonitor.specification.AlertEventSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.PredicateSpecification;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RegionQueryService {

    private final AlertEventRepository alertEventRepository;
    private final RegionRepository regionRepository;

    public List<RegionStatusResponse> getAllRegionStatuses(List<Long> regionIds, Boolean activeFilter) {
        List<Region> regions = resolveRegions(regionIds);

        return regions.stream()
                .map(this::toStatusResponse)
                .filter(status -> activeFilter == null || status.alertActive() == activeFilter)
                .toList();
    }

    public RegionStatusResponse getRegionStatus(Long regionId) {
        Region region = findRegionOrThrow(regionId);
        return toStatusResponse(region);
    }

    public PageResponse<AlertEventResponse> getRegionsHistory(
            List<Long> regionIds, LocalDateTime fromDate, LocalDateTime toDate, String timezone, Pageable pageable) {

        List<Region> regions = resolveRegions(regionIds);

        ZoneId zoneId = resolveZone(timezone);
        Instant from = (fromDate != null) ? fromDate.atZone(zoneId).toInstant() : null;
        Instant to = (toDate != null) ? toDate.atZone(zoneId).toInstant() : Instant.now();

        if (from != null && from.isAfter(to)) {
            throw new InvalidDateRangeException("'from' must not be after 'to'");
        }

        PredicateSpecification<AlertEvent> spec = AlertEventSpecifications.hasRegionIn(regions);
        if (from != null) {
            spec = spec.and(AlertEventSpecifications.startedBetween(from, to));
        }

        Page<AlertEvent> page = alertEventRepository.findBy(spec, query -> query.page(pageable));

        List<AlertEventResponse> content = page.getContent().stream()
                .map(event -> AlertEventResponse.from(event.getStartedAt(), event.getEndedAt(), event.getSource()))
                .toList();

        return new PageResponse<>(content, page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages(), page.isLast());
    }

    public List<RegionAlertStatsResponse> getAllRegionAlertStats(List<Long> regionIds,
                                                                 String period,
                                                                 LocalDateTime fromDate,
                                                                 LocalDateTime toDate,
                                                                 String timezone) {

        List<Region> regions = resolveRegions(regionIds);

        ZoneId zoneId = resolveZone(timezone);
        DateRange range = resolveDateRange(period, fromDate, toDate, zoneId);
        Instant now = Instant.now();

        List<AlertEvent> events = alertEventRepository.findAll(
                AlertEventSpecifications.hasRegionIn(regions)
                        .and(AlertEventSpecifications.startedBetween(range.from(), range.to())));

        var eventsByRegion = events.stream().collect(Collectors.groupingBy(AlertEvent::getRegion));

        return regions.stream()
                .map(region -> {
                    List<AlertEvent> regionEvents = eventsByRegion.getOrDefault(region, List.of());
                    return buildStatsResponse(timezone, region, zoneId, range, now, regionEvents);
                })
                .sorted(Comparator.comparingLong(RegionAlertStatsResponse::totalAlertSeconds).reversed())
                .toList();
    }

    public RegionAlertStatsResponse getRegionAlertStats(Long regionId,
                                                        String period,
                                                        LocalDateTime fromDate,
                                                        LocalDateTime toDate,
                                                        String timezone) {

        Region region = findRegionOrThrow(regionId);

        ZoneId zoneId = resolveZone(timezone);
        DateRange range = resolveDateRange(period, fromDate, toDate, zoneId);
        Instant now = Instant.now();

        List<AlertEvent> events = alertEventRepository.findAll(
                AlertEventSpecifications.hasRegionIn(List.of(region))
                        .and(AlertEventSpecifications.startedBetween(range.from(), range.to())));

        return buildStatsResponse(timezone, region, zoneId, range, now, events);
    }

    private List<Region> resolveRegions(List<Long> regionIds) {
        return (regionIds != null && !regionIds.isEmpty())
                ? regionRepository.findAllById(regionIds)
                : regionRepository.findAll();
    }

    private Region findRegionOrThrow(Long regionId) {
        return regionRepository.findById(regionId)
                .orElseThrow(() -> new RegionNotFoundException("Region not found with regionId: " + regionId));
    }

    private RegionAlertStatsResponse buildStatsResponse(String timezone,
                                                        Region region,
                                                        ZoneId zoneId,
                                                        DateRange range,
                                                        Instant now,
                                                        List<AlertEvent> events) {
        List<Long> durationInSeconds = events.stream()
                .map(event -> durationSeconds(event, now))
                .toList();

        long eventCount = events.size();
        long totalAlertSeconds = durationInSeconds.stream().mapToLong(Long::longValue).sum();
        long longestEventSeconds = durationInSeconds.stream().mapToLong(Long::longValue).max().orElse(0L);

        return new RegionAlertStatsResponse(
                region.getId(),
                region.getName(),
                ZonedDateTime.ofInstant(range.from(), zoneId),
                ZonedDateTime.ofInstant(range.to(), zoneId),
                timezone,
                eventCount,
                totalAlertSeconds,
                longestEventSeconds);
    }

    private record DateRange(Instant from, Instant to) {}

    private DateRange resolveDateRange(String period,
                                       LocalDateTime fromDate,
                                       LocalDateTime toDate,
                                       ZoneId zoneId) {
        Instant now = Instant.now();
        Instant effectiveTo = (toDate != null) ? toDate.atZone(zoneId).toInstant() : now;

        Instant effectiveFrom;
        if (fromDate != null) {
            effectiveFrom = fromDate.atZone(zoneId).toInstant();
        } else {
            effectiveFrom = switch ((period == null || period.isBlank()) ? "month" : period) {
                case "day" -> effectiveTo.minus(Duration.ofDays(1));
                case "week" -> effectiveTo.minus(Duration.ofDays(7));
                case "month" -> effectiveTo.minus(Duration.ofDays(30));
                case "year" -> effectiveTo.minus(Duration.ofDays(365));
                default -> throw new InvalidPeriodException("Unknown period: " + period);
            };
        }

        if (effectiveFrom.isAfter(effectiveTo)) {
            throw new InvalidDateRangeException("'from' must not be after 'to'");
        }

        return new DateRange(effectiveFrom, effectiveTo);
    }

    private long durationSeconds(AlertEvent event, Instant now) {
        Instant end = (event.getEndedAt() != null) ? event.getEndedAt() : now;
        return Duration.between(event.getStartedAt(), end).toSeconds();
    }

    private RegionStatusResponse toStatusResponse(Region region) {
        Optional<AlertEvent> openEvent = alertEventRepository.findByRegionAndEndedAtIsNull(region);
        boolean alertActive = openEvent.isPresent();
        var since = openEvent.map(AlertEvent::getStartedAt).orElse(null);
        return new RegionStatusResponse(region.getId(), region.getName(), alertActive, since);
    }

    private ZoneId resolveZone(String timezone) {
        try {
            return ZoneId.of(timezone);
        } catch (DateTimeException e) {
            throw new InvalidTimezoneException("Invalid timezone: " + timezone);
        }
    }
}