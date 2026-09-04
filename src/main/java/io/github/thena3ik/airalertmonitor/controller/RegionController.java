package io.github.thena3ik.airalertmonitor.controller;

import io.github.thena3ik.airalertmonitor.dto.common.PageResponse;
import io.github.thena3ik.airalertmonitor.dto.region.AlertEventResponse;
import io.github.thena3ik.airalertmonitor.dto.region.RegionAlertStatsResponse;
import io.github.thena3ik.airalertmonitor.dto.region.RegionStatusResponse;
import io.github.thena3ik.airalertmonitor.filter.RateLimitInterceptor;
import io.github.thena3ik.airalertmonitor.service.RegionQueryService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/regions")
@RequiredArgsConstructor
public class RegionController {

    private static final int MAX_PAGE_SIZE_PUBLIC = 100;
    private static final int MAX_PAGE_SIZE_TRUSTED = 1000;

    private final RegionQueryService regionQueryService;

    @GetMapping
    public List<RegionStatusResponse> getRegionsCurrentStatus(
            @RequestParam(name = "ids", required = false) List<Long> regionIds,
            @RequestParam(name = "names", required = false) List<String> regionNames,
            @RequestParam(name = "active", required = false) Boolean isActiveFilter,
            @RequestParam(name = "tz", defaultValue = "UTC") String timezone,
            @RequestParam(name = "lang", defaultValue = "ua") String lang) {
        return regionQueryService.getRegionsCurrentStatus(regionIds, regionNames, isActiveFilter, timezone, lang);
    }

    @GetMapping("/{id}")
    public RegionStatusResponse getRegionCurrentStatus(
            @PathVariable(name = "id") Long regionId,
            @RequestParam(name = "tz", defaultValue = "UTC") String timezone,
            @RequestParam(name = "lang", defaultValue = "ua") String lang) {
        return regionQueryService.getRegionCurrentStatus(regionId, lang, timezone);
    }

    @GetMapping("/history")
    public PageResponse<AlertEventResponse> getRegionsHistory(
            @RequestParam(name = "ids", required = false) List<Long> regionIds,
            @RequestParam(name = "names", required = false) List<String> regionNames,
            @RequestParam(name = "period", required = false) String period,
            @RequestParam(name = "from", required = false) OffsetDateTime fromDate,
            @RequestParam(name = "to", required = false) OffsetDateTime toDate,
            @RequestParam(name = "tz", defaultValue = "UTC") String timezone,
            @RequestParam(name = "lang", defaultValue = "ua") String lang,
            @PageableDefault(size = 20, sort = "startedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return regionQueryService.getRegionsHistory(
                regionIds, regionNames, period, fromDate, toDate, timezone, lang, capPageSize(pageable));
    }

    @GetMapping("/{id}/history")
    public PageResponse<AlertEventResponse> getRegionHistory(
            @PathVariable(name = "id") Long regionId,
            @RequestParam(name = "period", required = false) String period,
            @RequestParam(name = "from", required = false) OffsetDateTime fromDate,
            @RequestParam(name = "to", required = false) OffsetDateTime toDate,
            @RequestParam(name = "tz", defaultValue = "UTC") String timezone,
            @RequestParam(name = "lang", defaultValue = "ua") String lang,
            @PageableDefault(size = 20, sort = "startedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return regionQueryService.getRegionsHistory(
                List.of(regionId), null, period, fromDate, toDate, timezone, lang, capPageSize(pageable));
    }

    @GetMapping("/stats")
    public List<RegionAlertStatsResponse> getRegionsStatistics(
            @RequestParam(name = "ids", required = false) List<Long> regionIds,
            @RequestParam(name = "names", required = false) List<String> regionNames,
            @RequestParam(name = "period", required = false) String period,
            @RequestParam(name = "from", required = false) OffsetDateTime fromDate,
            @RequestParam(name = "to", required = false) OffsetDateTime toDate,
            @RequestParam(name = "tz", defaultValue = "UTC") String timezone,
            @RequestParam(name = "lang", defaultValue = "ua") String lang) {
        return regionQueryService.getRegionsStatistics(regionIds, regionNames, period, fromDate, toDate, timezone, lang);
    }

    @GetMapping("/{id}/stats")
    public RegionAlertStatsResponse getRegionStatistics(
            @PathVariable(name = "id") Long regionId,
            @RequestParam(name = "period", required = false) String period,
            @RequestParam(name = "from", required = false) OffsetDateTime fromDate,
            @RequestParam(name = "to", required = false) OffsetDateTime toDate,
            @RequestParam(name = "tz", defaultValue = "UTC") String timezone,
            @RequestParam(name = "lang", defaultValue = "ua") String lang) {
        return regionQueryService.getRegionStatistics(regionId, period, fromDate, toDate, timezone, lang);
    }

    private Pageable capPageSize(Pageable pageable) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        boolean isTrusted = Boolean.TRUE.equals(request.getAttribute(RateLimitInterceptor.TRUSTED_CALLER_ATTR));
        int maxAllowed = isTrusted ? MAX_PAGE_SIZE_TRUSTED : MAX_PAGE_SIZE_PUBLIC;

        if (pageable.getPageSize() > maxAllowed) {
            return PageRequest.of(pageable.getPageNumber(), maxAllowed, pageable.getSort());
        }
        return pageable;
    }
}