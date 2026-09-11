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

@Tag(name = "Regions", description = "Query current alert status, history and statistics. Public access is limited to 60 requests/minute.")
@RestController
@RequestMapping("/api/v1/regions")
@RequiredArgsConstructor
public class RegionController {

    private static final int MAX_PAGE_SIZE_PUBLIC = 100;
    private static final int MAX_PAGE_SIZE_TRUSTED = 1000;

    private final RegionQueryService regionQueryService;

    @Operation(
            summary = "Get current alert status for regions",
            description = "Returns the current alert status for all regions, optionally filtered by id, name, "
                    + "or active state. If neither `ids` nor `names` are provided, all regions are returned."
    )
    @ApiResponse(responseCode = "200", description = "List of region statuses")
    @ApiResponse(responseCode = "400", description = "Both `ids` and `names` provided, or `tz` is not a valid IANA timezone",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                    examples = {
                            @ExampleObject(name = "invalidFilter", ref = "#/components/examples/invalidFilter"),
                            @ExampleObject(name = "invalidTimezone", ref = "#/components/examples/invalidTimezone")
                    }))
    @GetMapping
    public List<RegionStatusResponse> getRegionsCurrentStatus(
            @Parameter(description = "Filter by region id. Cannot be combined with `names`.")
            @RequestParam(name = "ids", required = false) List<Long> regionIds,
            @Parameter(description = "Filter by region name. Cannot be combined with `ids`.")
            @RequestParam(name = "names", required = false) List<String> regionNames,
            @Parameter(description = "If set, only return regions whose alert-active state matches this value")
            @RequestParam(name = "active", required = false) Boolean isActiveFilter,
            @Parameter(description = "IANA timezone used to format the `since` timestamp, e.g. `Europe/Kyiv`")
            @RequestParam(name = "tz", defaultValue = "UTC") String timezone,
            @Parameter(description = "Language for the region name: `ua` or `en`")
            @RequestParam(name = "lang", defaultValue = "ua") String lang) {
        return regionQueryService.getRegionsCurrentStatus(regionIds, regionNames, isActiveFilter, timezone, lang);
    }

    @Operation(
            summary = "Get current alert status for a single region",
            description = "Returns the current alert status for the region with the given id."
    )
    @ApiResponse(responseCode = "200", description = "Region status")
    @ApiResponse(responseCode = "400", description = "`tz` is not a valid IANA timezone",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(name = "invalidTimezone", ref = "#/components/examples/invalidTimezone")))
    @ApiResponse(responseCode = "404", description = "Region not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(name = "regionNotFound", ref = "#/components/examples/regionNotFound")))
    @GetMapping("/{id}")
    public RegionStatusResponse getRegionCurrentStatus(
            @Parameter(description = "Region id", required = true)
            @PathVariable(name = "id") Long regionId,
            @Parameter(description = "IANA timezone used to format the `since` timestamp")
            @RequestParam(name = "tz", defaultValue = "UTC") String timezone,
            @Parameter(description = "Language for the region name: `ua` or `en`")
            @RequestParam(name = "lang", defaultValue = "ua") String lang) {
        return regionQueryService.getRegionCurrentStatus(regionId, lang, timezone);
    }

    @Operation(
            summary = "Get alert history across regions",
            description = "Returns a paginated list of alert events across all regions, or a filtered subset. "
                    + "Use either `period` (relative window, e.g. `day`, `week`, `month`, `year`, `all`) "
                    + "or an explicit `from`/`to` range. Page size is capped at " + MAX_PAGE_SIZE_PUBLIC
                    + " for public callers (60 RPM) and " + MAX_PAGE_SIZE_TRUSTED + " for trusted callers."
    )
    @ApiResponse(responseCode = "200", description = "Page of alert events")
    @ApiResponse(responseCode = "400", description = "Both `ids` and `names` provided, `tz` is invalid, "
            + "`period` is unrecognized, or `from` is after `to`",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                    examples = {
                            @ExampleObject(name = "invalidFilter", ref = "#/components/examples/invalidFilter"),
                            @ExampleObject(name = "invalidTimezone", ref = "#/components/examples/invalidTimezone"),
                            @ExampleObject(name = "invalidPeriod", ref = "#/components/examples/invalidPeriod"),
                            @ExampleObject(name = "invalidDateRange", ref = "#/components/examples/invalidDateRange")
                    }))
    @GetMapping("/history")
    public PageResponse<AlertEventResponse> getRegionsHistory(
            @Parameter(description = "Filter by region id. Cannot be combined with `names`.")
            @RequestParam(name = "ids", required = false) List<Long> regionIds,
            @Parameter(description = "Filter by region name. Cannot be combined with `ids`.")
            @RequestParam(name = "names", required = false) List<String> regionNames,
            @Parameter(description = "Relative time window: `day`, `week`, `month`, `year`, or `all`. Ignored if `from` is set.")
            @RequestParam(name = "period", required = false) String period,
            @Parameter(description = "Start of an explicit date range (ISO-8601 offset date-time)")
            @RequestParam(name = "from", required = false) OffsetDateTime fromDate,
            @Parameter(description = "End of an explicit date range (ISO-8601 offset date-time). Defaults to now.")
            @RequestParam(name = "to", required = false) OffsetDateTime toDate,
            @Parameter(description = "IANA timezone used to format event timestamps")
            @RequestParam(name = "tz", defaultValue = "UTC") String timezone,
            @Parameter(description = "Language for the region name: `ua` or `en`")
            @RequestParam(name = "lang", defaultValue = "ua") String lang,
            @Parameter(description = "Pagination and sorting. Default sort is `startedAt` descending.")
            @PageableDefault(size = 20, sort = "startedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return regionQueryService.getRegionsHistory(
                regionIds, regionNames, period, fromDate, toDate, timezone, lang, capPageSize(pageable));
    }

    @Operation(
            summary = "Get alert history for a single region",
            description = "Returns a paginated list of alert events for one region. Same filtering rules as "
                    + "`/history` apply for `period`/`from`/`to`."
    )
    @ApiResponse(responseCode = "200", description = "Page of alert events for the region")
    @ApiResponse(responseCode = "400", description = "`tz` is invalid, `period` is unrecognized, or `from` is after `to`",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                    examples = {
                            @ExampleObject(name = "invalidTimezone", ref = "#/components/examples/invalidTimezone"),
                            @ExampleObject(name = "invalidPeriod", ref = "#/components/examples/invalidPeriod"),
                            @ExampleObject(name = "invalidDateRange", ref = "#/components/examples/invalidDateRange")
                    }))
    @ApiResponse(responseCode = "404", description = "Region not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(name = "regionNotFound", ref = "#/components/examples/regionNotFound")))
    @GetMapping("/{id}/history")
    public PageResponse<AlertEventResponse> getRegionHistory(
            @Parameter(description = "Region id", required = true)
            @PathVariable(name = "id") Long regionId,
            @Parameter(description = "Relative time window: `day`, `week`, `month`, `year`, or `all`. Ignored if `from` is set.")
            @RequestParam(name = "period", required = false) String period,
            @Parameter(description = "Start of an explicit date range (ISO-8601 offset date-time)")
            @RequestParam(name = "from", required = false) OffsetDateTime fromDate,
            @Parameter(description = "End of an explicit date range (ISO-8601 offset date-time). Defaults to now.")
            @RequestParam(name = "to", required = false) OffsetDateTime toDate,
            @Parameter(description = "IANA timezone used to format event timestamps")
            @RequestParam(name = "tz", defaultValue = "UTC") String timezone,
            @Parameter(description = "Language for the region name: `ua` or `en`")
            @RequestParam(name = "lang", defaultValue = "ua") String lang,
            @Parameter(description = "Pagination and sorting. Default sort is `startedAt` descending.")
            @PageableDefault(size = 20, sort = "startedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return regionQueryService.getRegionHistory(regionId, period, fromDate, toDate, timezone, lang, capPageSize(pageable));
    }

    @Operation(
            summary = "Get alert statistics across regions",
            description = "Returns aggregated alert statistics (event count, total and longest alert duration) "
                    + "for each region over the requested time window, sorted by total alert time descending."
    )
    @ApiResponse(responseCode = "200", description = "List of per-region statistics")
    @ApiResponse(responseCode = "400", description = "Both `ids` and `names` provided, `tz` is invalid, "
            + "`period` is unrecognized, or `from` is after `to`",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                    examples = {
                            @ExampleObject(name = "invalidFilter", ref = "#/components/examples/invalidFilter"),
                            @ExampleObject(name = "invalidTimezone", ref = "#/components/examples/invalidTimezone"),
                            @ExampleObject(name = "invalidPeriod", ref = "#/components/examples/invalidPeriod"),
                            @ExampleObject(name = "invalidDateRange", ref = "#/components/examples/invalidDateRange")
                    }))
    @GetMapping("/stats")
    public List<RegionAlertStatsResponse> getRegionsStatistics(
            @Parameter(description = "Filter by region id. Cannot be combined with `names`.")
            @RequestParam(name = "ids", required = false) List<Long> regionIds,
            @Parameter(description = "Filter by region name. Cannot be combined with `ids`.")
            @RequestParam(name = "names", required = false) List<String> regionNames,
            @Parameter(description = "Relative time window: `day`, `week`, `month`, `year`, or `all`. Ignored if `from` is set.")
            @RequestParam(name = "period", required = false) String period,
            @Parameter(description = "Start of an explicit date range (ISO-8601 offset date-time)")
            @RequestParam(name = "from", required = false) OffsetDateTime fromDate,
            @Parameter(description = "End of an explicit date range (ISO-8601 offset date-time). Defaults to now.")
            @RequestParam(name = "to", required = false) OffsetDateTime toDate,
            @Parameter(description = "IANA timezone used to format the `from`/`to` timestamps in the response")
            @RequestParam(name = "tz", defaultValue = "UTC") String timezone,
            @Parameter(description = "Language for the region name: `ua` or `en`")
            @RequestParam(name = "lang", defaultValue = "ua") String lang) {
        return regionQueryService.getRegionsStatistics(regionIds, regionNames, period, fromDate, toDate, timezone, lang);
    }

    @Operation(
            summary = "Get alert statistics for a single region",
            description = "Returns aggregated alert statistics (event count, total and longest alert duration) "
                    + "for one region over the requested time window."
    )
    @ApiResponse(responseCode = "200", description = "Region statistics")
    @ApiResponse(responseCode = "400", description = "`tz` is invalid, `period` is unrecognized, or `from` is after `to`",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                    examples = {
                            @ExampleObject(name = "invalidTimezone", ref = "#/components/examples/invalidTimezone"),
                            @ExampleObject(name = "invalidPeriod", ref = "#/components/examples/invalidPeriod"),
                            @ExampleObject(name = "invalidDateRange", ref = "#/components/examples/invalidDateRange")
                    }))
    @ApiResponse(responseCode = "404", description = "Region not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(name = "regionNotFound", ref = "#/components/examples/regionNotFound")))
    @GetMapping("/{id}/stats")
    public RegionAlertStatsResponse getRegionStatistics(
            @Parameter(description = "Region id", required = true)
            @PathVariable(name = "id") Long regionId,
            @Parameter(description = "Relative time window: `day`, `week`, `month`, `year`, or `all`. Ignored if `from` is set.")
            @RequestParam(name = "period", required = false) String period,
            @Parameter(description = "Start of an explicit date range (ISO-8601 offset date-time)")
            @RequestParam(name = "from", required = false) OffsetDateTime fromDate,
            @Parameter(description = "End of an explicit date range (ISO-8601 offset date-time). Defaults to now.")
            @RequestParam(name = "to", required = false) OffsetDateTime toDate,
            @Parameter(description = "IANA timezone used to format the `from`/`to` timestamps in the response")
            @RequestParam(name = "tz", defaultValue = "UTC") String timezone,
            @Parameter(description = "Language for the region name: `ua` or `en`")
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