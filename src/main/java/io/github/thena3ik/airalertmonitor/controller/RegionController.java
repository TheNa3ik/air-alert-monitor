package io.github.thena3ik.airalertmonitor.controller;

import io.github.thena3ik.airalertmonitor.dto.common.PageResponse;
import io.github.thena3ik.airalertmonitor.dto.region.AlertEventResponse;
import io.github.thena3ik.airalertmonitor.dto.region.RegionAlertStatsResponse;
import io.github.thena3ik.airalertmonitor.dto.region.RegionStatusResponse;
import io.github.thena3ik.airalertmonitor.service.RegionQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/regions")
@RequiredArgsConstructor
public class RegionController {

    private static final int MAX_PAGE_SIZE = 100;

    private final RegionQueryService regionQueryService;

    @GetMapping
    public List<RegionStatusResponse> getAllRegions(
            @RequestParam(required = false) List<Long> ids,
            @RequestParam(required = false) List<String> names,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "ua") String lang) {
        return regionQueryService.getAllRegionStatuses(ids, names, active, lang);
    }

    @GetMapping("/{id}")
    public RegionStatusResponse getRegionById(
            @PathVariable Long id,
            @RequestParam(defaultValue = "ua") String lang) {
        return regionQueryService.getRegionStatus(id, lang);
    }

    @GetMapping("/history")
    public PageResponse<AlertEventResponse> getRegionsHistory(
            @RequestParam(required = false) List<Long> ids,
            @RequestParam(required = false) List<String> names,
            @RequestParam(required = false) LocalDateTime from,
            @RequestParam(required = false) LocalDateTime to,
            @RequestParam(defaultValue = "UTC") String tz,
            @PageableDefault(size = 20, sort = "startedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return regionQueryService.getRegionsHistory(ids, names, from, to, tz, capPageSize(pageable));
    }

    @GetMapping("/{id}/history")
    public PageResponse<AlertEventResponse> getRegionHistory(
            @PathVariable Long id,
            @RequestParam(required = false) LocalDateTime from,
            @RequestParam(required = false) LocalDateTime to,
            @RequestParam(defaultValue = "UTC") String tz,
            @PageableDefault(size = 20, sort = "startedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return regionQueryService.getRegionsHistory(List.of(id), null, from, to, tz, capPageSize(pageable));
    }

    @GetMapping("/stats")
    public List<RegionAlertStatsResponse> getAllRegionStats(
            @RequestParam(required = false) List<Long> ids,
            @RequestParam(required = false) List<String> names,
            @RequestParam(required = false) String period,
            @RequestParam(required = false) LocalDateTime from,
            @RequestParam(required = false) LocalDateTime to,
            @RequestParam(defaultValue = "UTC") String tz,
            @RequestParam(defaultValue = "ua") String lang) {
        return regionQueryService.getAllRegionAlertStats(ids, names, period, from, to, tz, lang);
    }

    @GetMapping("/{id}/stats")
    public RegionAlertStatsResponse getRegionStats(
            @PathVariable Long id,
            @RequestParam(required = false) String period,
            @RequestParam(required = false) LocalDateTime from,
            @RequestParam(required = false) LocalDateTime to,
            @RequestParam(defaultValue = "UTC") String tz,
            @RequestParam(defaultValue = "ua") String lang) {
        return regionQueryService.getRegionAlertStats(id, period, from, to, tz, lang);
    }

    private Pageable capPageSize(Pageable pageable) {
        if (pageable.getPageSize() > MAX_PAGE_SIZE) {
            return PageRequest.of(pageable.getPageNumber(), MAX_PAGE_SIZE, pageable.getSort());
        }
        return pageable;
    }
}