package io.github.thena3ik.airalertmonitor.controller;

import io.github.thena3ik.airalertmonitor.dto.AlertEventResponse;
import io.github.thena3ik.airalertmonitor.dto.PageResponse;
import io.github.thena3ik.airalertmonitor.dto.RegionStatusResponse;
import io.github.thena3ik.airalertmonitor.service.RegionQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/regions")
@RequiredArgsConstructor
public class RegionController {

    private final RegionQueryService regionQueryService;

    @GetMapping
    public List<RegionStatusResponse> getAllRegions(
            @RequestParam(required = false) List<Long> ids,
            @RequestParam(required = false) Boolean active) {
        return regionQueryService.getAllRegionStatuses(ids, active);
    }

    @GetMapping("/{id}")
    public RegionStatusResponse getRegionById(@PathVariable Long id) {
        return regionQueryService.getRegionStatus(id);
    }

    @GetMapping("/{id}/history")
    public PageResponse<AlertEventResponse> getRegionHistory(@PathVariable Long id, Pageable pageable) {
        return regionQueryService.getRegionHistory(id, pageable);
    }
}
