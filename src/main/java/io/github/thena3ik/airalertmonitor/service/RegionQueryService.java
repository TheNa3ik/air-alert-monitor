package io.github.thena3ik.airalertmonitor.service;

import io.github.thena3ik.airalertmonitor.dto.common.PageResponse;
import io.github.thena3ik.airalertmonitor.dto.region.AlertEventResponse;
import io.github.thena3ik.airalertmonitor.dto.region.RegionAlertStatsResponse;
import io.github.thena3ik.airalertmonitor.dto.region.RegionStatusResponse;
import io.github.thena3ik.airalertmonitor.entity.AlertEvent;
import io.github.thena3ik.airalertmonitor.entity.Region;
import io.github.thena3ik.airalertmonitor.exception.*;
import io.github.thena3ik.airalertmonitor.repository.AlertEventRepository;
import io.github.thena3ik.airalertmonitor.repository.RegionRepository;
import io.github.thena3ik.airalertmonitor.specification.AlertEventSpecifications;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.PredicateSpecification;
import org.springframework.data.jpa.domain.Specification;
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

    public List<RegionStatusResponse> getRegionsCurrentStatus(List<Long> regionIds,
                                                              List<String> regionNames,
                                                              Boolean isActiveFilter,
                                                              String timezone,
                                                              String lang) {
        List<Region> regions = resolveRegions(regionIds, regionNames);
        ZoneId zoneId = resolveTimezone(timezone);

        return regions.stream()
                .map(region -> toCurrentStatusResponse(region, lang, zoneId))
                .filter(status -> isActiveFilter == null || status.alertActive() == isActiveFilter)
                .toList();
    }

    public RegionStatusResponse getRegionCurrentStatus(Long regionId,
                                                       String lang,
                                                       String timezone) {
        Region region = findRegionOrThrow(regionId);
        ZoneId zoneId = resolveTimezone(timezone);
        return toCurrentStatusResponse(region, lang, zoneId);
    }

    public PageResponse<AlertEventResponse> getRegionsHistory(List<Long> regionIds,
                                                              List<String> regionNames,
                                                              OffsetDateTime fromDate,
                                                              OffsetDateTime toDate,
                                                              String timezone,
                                                              Pageable pageable) {

        List<Region> regions = resolveRegions(regionIds, regionNames);

        ZoneId zoneId = resolveTimezone(timezone);
        Instant fromInstant = (fromDate != null) ? fromDate.toInstant() : null;
        Instant toInstant = (toDate != null) ? toDate.toInstant() : Instant.now();

        if (fromInstant != null && fromInstant.isAfter(toInstant)) {
            throw new InvalidDateRangeException("'from' must not be after 'to'");
        }

        PredicateSpecification<AlertEvent> specification = AlertEventSpecifications.hasRegionIn(regions);
        if (fromInstant != null) {
            specification = specification.and(AlertEventSpecifications.startedBetween(fromInstant, toInstant));
        }

        Page<AlertEvent> page = alertEventRepository.findBy(specification, query -> query.page(pageable));

        List<AlertEventResponse> content = page.getContent().stream()
                .map(event -> {
                    ZonedDateTime startZoned = event.getStartedAt().atZone(zoneId);
                    ZonedDateTime endZoned = (event.getEndedAt() != null) ? event.getEndedAt().atZone(zoneId) : null;
                    return AlertEventResponse.from(startZoned, endZoned, event.getSource());
                })
                .toList();

        return new PageResponse<>(content, page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages(), page.isLast());
    }

    public List<RegionAlertStatsResponse> getRegionsStatistics(List<Long> regionIds,
                                                               List<String> regionNames,
                                                               String period,
                                                               OffsetDateTime fromDate,
                                                               OffsetDateTime toDate,
                                                               String timezone,
                                                               String lang) {

        List<Region> regions = resolveRegions(regionIds, regionNames);

        ZoneId zoneId = resolveTimezone(timezone);
        DateRange dateRange = resolveDateRange(period, fromDate, toDate);
        Instant currentInstant = Instant.now();

        List<AlertEvent> events = alertEventRepository.findAll(
                AlertEventSpecifications.hasRegionIn(regions)
                        .and(AlertEventSpecifications.startedBetween(dateRange.fromInstant(), dateRange.toInstant())));

        var eventsGroupedByRegion = events.stream().collect(Collectors.groupingBy(AlertEvent::getRegion));

        return regions.stream()
                .map(region -> {
                    List<AlertEvent> regionEvents = eventsGroupedByRegion.getOrDefault(region, List.of());
                    return toStatisticsResponse(timezone, region, zoneId, dateRange, currentInstant, regionEvents, lang);
                })
                .sorted(Comparator.comparingLong(RegionAlertStatsResponse::totalAlertSeconds).reversed())
                .toList();
    }

    public RegionAlertStatsResponse getRegionStatistics(Long regionId,
                                                        String period,
                                                        OffsetDateTime fromDate,
                                                        OffsetDateTime toDate,
                                                        String timezone,
                                                        String lang) {

        Region region = findRegionOrThrow(regionId);

        ZoneId zoneId = resolveTimezone(timezone);
        DateRange dateRange = resolveDateRange(period, fromDate, toDate);
        Instant currentInstant = Instant.now();

        List<AlertEvent> events = alertEventRepository.findAll(
                AlertEventSpecifications.hasRegionIn(List.of(region))
                        .and(AlertEventSpecifications.startedBetween(dateRange.fromInstant(), dateRange.toInstant())));

        return toStatisticsResponse(timezone, region, zoneId, dateRange, currentInstant, events, lang);
    }

    private List<Region> resolveRegions(List<Long> regionIds, List<String> regionNames) {
        boolean hasIds = regionIds != null && !regionIds.isEmpty();
        boolean hasNames = regionNames != null && !regionNames.isEmpty();

        if (hasIds && hasNames) {
            throw new InvalidFilterException("Cannot filter by both 'ids' and 'names' simultaneously.");
        }

        if (!hasIds && !hasNames) {
            return regionRepository.findAll();
        }

        Specification<Region> specification;

        if (hasIds) {
            specification = (root, query, cb) -> root.get("id").in(regionIds);
        } else {
            specification = (root, query, cb) -> {
                Predicate inNameUa = root.get("name").in(regionNames);
                Predicate inNameEn = root.get("nameEn").in(regionNames);
                return cb.or(inNameUa, inNameEn);
            };
        }

        return regionRepository.findAll(specification);
    }

    private String getLocalizedName(Region region, String lang) {
        return "en".equalsIgnoreCase(lang) ? region.getNameEn() : region.getName();
    }

    private Region findRegionOrThrow(Long regionId) {
        return regionRepository.findById(regionId)
                .orElseThrow(() -> new RegionNotFoundException("Region not found with regionId: " + regionId));
    }

    private RegionAlertStatsResponse toStatisticsResponse(String timezone,
                                                          Region region,
                                                          ZoneId zoneId,
                                                          DateRange dateRange,
                                                          Instant currentInstant,
                                                          List<AlertEvent> events,
                                                          String lang) {
        List<Long> durationInSeconds = events.stream()
                .map(event -> calculateDurationInSeconds(event, currentInstant))
                .toList();

        long eventCount = events.size();
        long totalAlertSeconds = durationInSeconds.stream().mapToLong(Long::longValue).sum();
        long longestEventSeconds = durationInSeconds.stream().mapToLong(Long::longValue).max().orElse(0L);

        return new RegionAlertStatsResponse(
                region.getId(),
                getLocalizedName(region, lang),
                ZonedDateTime.ofInstant(dateRange.fromInstant(), zoneId),
                ZonedDateTime.ofInstant(dateRange.toInstant(), zoneId),
                timezone,
                eventCount,
                totalAlertSeconds,
                longestEventSeconds);
    }

    private record DateRange(Instant fromInstant, Instant toInstant) {}

    private DateRange resolveDateRange(String period,
                                       OffsetDateTime fromDate,
                                       OffsetDateTime toDate) {
        Instant currentInstant = Instant.now();
        Instant effectiveToInstant = (toDate != null) ? toDate.toInstant() : currentInstant;

        Instant effectiveFromInstant;
        if (fromDate != null) {
            effectiveFromInstant = fromDate.toInstant();
        } else {
            effectiveFromInstant = switch ((period == null || period.isBlank()) ? "month" : period) {
                case "day" -> effectiveToInstant.minus(Duration.ofDays(1));
                case "week" -> effectiveToInstant.minus(Duration.ofDays(7));
                case "month" -> effectiveToInstant.minus(Duration.ofDays(30));
                case "year" -> effectiveToInstant.minus(Duration.ofDays(365));
                default -> throw new InvalidPeriodException("Unknown period: " + period);
            };
        }

        if (effectiveFromInstant.isAfter(effectiveToInstant)) {
            throw new InvalidDateRangeException("'from' must not be after 'to'");
        }

        return new DateRange(effectiveFromInstant, effectiveToInstant);
    }

    private long calculateDurationInSeconds(AlertEvent event, Instant currentInstant) {
        Instant endInstant = (event.getEndedAt() != null) ? event.getEndedAt() : currentInstant;
        return Duration.between(event.getStartedAt(), endInstant).toSeconds();
    }

    private RegionStatusResponse toCurrentStatusResponse(Region region,
                                                         String lang,
                                                         ZoneId zoneId) {
        Optional<AlertEvent> openEvent = alertEventRepository.findByRegionAndEndedAtIsNull(region);
        boolean isAlertActive = openEvent.isPresent();

        ZonedDateTime startedSince = openEvent
                .map(AlertEvent::getStartedAt)
                .map(instant -> instant.atZone(zoneId))
                .orElse(null);

        return new RegionStatusResponse(region.getId(), getLocalizedName(region, lang), isAlertActive, startedSince);
    }

    private ZoneId resolveTimezone(String timezone) {
        try {
            return ZoneId.of(timezone);
        } catch (DateTimeException e) {
            throw new InvalidTimezoneException("Invalid timezone: " + timezone);
        }
    }
}