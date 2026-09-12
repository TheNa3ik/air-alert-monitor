package io.github.thena3ik.airalertmonitor.controller;

import io.github.thena3ik.airalertmonitor.dto.common.ErrorResponse;
import io.github.thena3ik.airalertmonitor.dto.common.PageResponse;
import io.github.thena3ik.airalertmonitor.dto.region.AlertEventResponse;
import io.github.thena3ik.airalertmonitor.dto.region.RegionAlertStatsResponse;
import io.github.thena3ik.airalertmonitor.dto.region.RegionStatusResponse;
import io.github.thena3ik.airalertmonitor.filter.RateLimitInterceptor;
import io.github.thena3ik.airalertmonitor.service.RegionQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Regions", description = "api.region.tag.desc")
@RestController
@RequestMapping("/api/v1/regions")
@RequiredArgsConstructor
public class RegionController {

    private static final int MAX_PAGE_SIZE_PUBLIC = 100;
    private static final int MAX_PAGE_SIZE_TRUSTED = 1000;

    private final RegionQueryService regionQueryService;

    @Operation(summary = "api.region.current.all.summary", description = "api.region.current.all.desc")
    @ApiResponse(responseCode = "200", description = "api.res.regionStatusList")
    @ApiResponse(responseCode = "400", description = "api.err.400.filterOrTz",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                    examples = {
                            @ExampleObject(name = "invalidFilter", ref = "#/components/examples/invalidFilter"),
                            @ExampleObject(name = "invalidTimezone", ref = "#/components/examples/invalidTimezone")
                    }))
    @GetMapping
    public List<RegionStatusResponse> getRegionsCurrentStatus(
            @Parameter(description = "api.param.ids") @RequestParam(name = "ids", required = false) List<Long> regionIds,
            @Parameter(description = "api.param.names") @RequestParam(name = "names", required = false) List<String> regionNames,
            @Parameter(description = "api.param.active") @RequestParam(name = "active", required = false) Boolean isActiveFilter,
            @Parameter(description = "api.param.tz.since") @RequestParam(name = "tz", defaultValue = "UTC") String timezone,
            @Parameter(description = "api.param.lang") @RequestParam(name = "lang", defaultValue = "ua") String lang) {
        return regionQueryService.getRegionsCurrentStatus(regionIds, regionNames, isActiveFilter, timezone, lang);
    }

    @Operation(summary = "api.region.current.single.summary", description = "api.region.current.single.desc")
    @ApiResponse(responseCode = "200", description = "api.res.regionStatus")
    @ApiResponse(responseCode = "400", description = "api.err.400.tz",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(name = "invalidTimezone", ref = "#/components/examples/invalidTimezone")))
    @ApiResponse(responseCode = "404", description = "api.err.404.regionNotFound",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(name = "regionNotFound", ref = "#/components/examples/regionNotFound")))
    @GetMapping("/{id}")
    public RegionStatusResponse getRegionCurrentStatus(
            @Parameter(description = "api.param.regionId", required = true) @PathVariable(name = "id") Long regionId,
            @Parameter(description = "api.param.tz.since") @RequestParam(name = "tz", defaultValue = "UTC") String timezone,
            @Parameter(description = "api.param.lang") @RequestParam(name = "lang", defaultValue = "ua") String lang) {
        return regionQueryService.getRegionCurrentStatus(regionId, lang, timezone);
    }

    @Operation(summary = "api.region.history.all.summary", description = "api.region.history.all.desc")
    @ApiResponse(responseCode = "200", description = "api.res.pageAlertEvents")
    @ApiResponse(responseCode = "400", description = "api.err.400.historyFilters",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                    examples = {
                            @ExampleObject(name = "invalidFilter", ref = "#/components/examples/invalidFilter"),
                            @ExampleObject(name = "invalidTimezone", ref = "#/components/examples/invalidTimezone"),
                            @ExampleObject(name = "invalidPeriod", ref = "#/components/examples/invalidPeriod"),
                            @ExampleObject(name = "invalidDateRange", ref = "#/components/examples/invalidDateRange")
                    }))
    @GetMapping("/history")
    public PageResponse<AlertEventResponse> getRegionsHistory(
            @Parameter(description = "api.param.ids") @RequestParam(name = "ids", required = false) List<Long> regionIds,
            @Parameter(description = "api.param.names") @RequestParam(name = "names", required = false) List<String> regionNames,
            @Parameter(description = "api.param.period") @RequestParam(name = "period", required = false) String period,
            @Parameter(description = "api.param.from") @RequestParam(name = "from", required = false) OffsetDateTime fromDate,
            @Parameter(description = "api.param.to") @RequestParam(name = "to", required = false) OffsetDateTime toDate,
            @Parameter(description = "api.param.tz.event") @RequestParam(name = "tz", defaultValue = "UTC") String timezone,
            @Parameter(description = "api.param.lang") @RequestParam(name = "lang", defaultValue = "ua") String lang,
            @Parameter(description = "api.param.pageable") @PageableDefault(size = 20, sort = "startedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return regionQueryService.getRegionsHistory(regionIds, regionNames, period, fromDate, toDate, timezone, lang, capPageSize(pageable));
    }

    @Operation(summary = "api.region.history.single.summary", description = "api.region.history.single.desc")
    @ApiResponse(responseCode = "200", description = "api.res.pageAlertEventsRegion")
    @ApiResponse(responseCode = "400", description = "api.err.400.historyFiltersSingle",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                    examples = {
                            @ExampleObject(name = "invalidTimezone", ref = "#/components/examples/invalidTimezone"),
                            @ExampleObject(name = "invalidPeriod", ref = "#/components/examples/invalidPeriod"),
                            @ExampleObject(name = "invalidDateRange", ref = "#/components/examples/invalidDateRange")
                    }))
    @ApiResponse(responseCode = "404", description = "api.err.404.regionNotFound",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(name = "regionNotFound", ref = "#/components/examples/regionNotFound")))
    @GetMapping("/{id}/history")
    public PageResponse<AlertEventResponse> getRegionHistory(
            @Parameter(description = "api.param.regionId", required = true) @PathVariable(name = "id") Long regionId,
            @Parameter(description = "api.param.period") @RequestParam(name = "period", required = false) String period,
            @Parameter(description = "api.param.from") @RequestParam(name = "from", required = false) OffsetDateTime fromDate,
            @Parameter(description = "api.param.to") @RequestParam(name = "to", required = false) OffsetDateTime toDate,
            @Parameter(description = "api.param.tz.event") @RequestParam(name = "tz", defaultValue = "UTC") String timezone,
            @Parameter(description = "api.param.lang") @RequestParam(name = "lang", defaultValue = "ua") String lang,
            @Parameter(description = "api.param.pageable") @PageableDefault(size = 20, sort = "startedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return regionQueryService.getRegionHistory(regionId, period, fromDate, toDate, timezone, lang, capPageSize(pageable));
    }

    @Operation(summary = "api.region.stats.all.summary", description = "api.region.stats.all.desc")
    @ApiResponse(responseCode = "200", description = "api.res.listRegionStats")
    @ApiResponse(responseCode = "400", description = "api.err.400.historyFilters",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                    examples = {
                            @ExampleObject(name = "invalidFilter", ref = "#/components/examples/invalidFilter"),
                            @ExampleObject(name = "invalidTimezone", ref = "#/components/examples/invalidTimezone"),
                            @ExampleObject(name = "invalidPeriod", ref = "#/components/examples/invalidPeriod"),
                            @ExampleObject(name = "invalidDateRange", ref = "#/components/examples/invalidDateRange")
                    }))
    @GetMapping("/stats")
    public List<RegionAlertStatsResponse> getRegionsStatistics(
            @Parameter(description = "api.param.ids") @RequestParam(name = "ids", required = false) List<Long> regionIds,
            @Parameter(description = "api.param.names") @RequestParam(name = "names", required = false) List<String> regionNames,
            @Parameter(description = "api.param.period") @RequestParam(name = "period", required = false) String period,
            @Parameter(description = "api.param.from") @RequestParam(name = "from", required = false) OffsetDateTime fromDate,
            @Parameter(description = "api.param.to") @RequestParam(name = "to", required = false) OffsetDateTime toDate,
            @Parameter(description = "api.param.tz.stats") @RequestParam(name = "tz", defaultValue = "UTC") String timezone,
            @Parameter(description = "api.param.lang") @RequestParam(name = "lang", defaultValue = "ua") String lang) {
        return regionQueryService.getRegionsStatistics(regionIds, regionNames, period, fromDate, toDate, timezone, lang);
    }

    @Operation(summary = "api.region.stats.single.summary", description = "api.region.stats.single.desc")
    @ApiResponse(responseCode = "200", description = "api.res.regionStats")
    @ApiResponse(responseCode = "400", description = "api.err.400.historyFiltersSingle",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                    examples = {
                            @ExampleObject(name = "invalidTimezone", ref = "#/components/examples/invalidTimezone"),
                            @ExampleObject(name = "invalidPeriod", ref = "#/components/examples/invalidPeriod"),
                            @ExampleObject(name = "invalidDateRange", ref = "#/components/examples/invalidDateRange")
                    }))
    @ApiResponse(responseCode = "404", description = "api.err.404.regionNotFound",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(name = "regionNotFound", ref = "#/components/examples/regionNotFound")))
    @GetMapping("/{id}/stats")
    public RegionAlertStatsResponse getRegionStatistics(
            @Parameter(description = "api.param.regionId", required = true) @PathVariable(name = "id") Long regionId,
            @Parameter(description = "api.param.period") @RequestParam(name = "period", required = false) String period,
            @Parameter(description = "api.param.from") @RequestParam(name = "from", required = false) OffsetDateTime fromDate,
            @Parameter(description = "api.param.to") @RequestParam(name = "to", required = false) OffsetDateTime toDate,
            @Parameter(description = "api.param.tz.stats") @RequestParam(name = "tz", defaultValue = "UTC") String timezone,
            @Parameter(description = "api.param.lang") @RequestParam(name = "lang", defaultValue = "ua") String lang) {
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