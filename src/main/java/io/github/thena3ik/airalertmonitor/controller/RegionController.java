package io.github.thena3ik.airalertmonitor.controller;

import io.github.thena3ik.airalertmonitor.dto.RegionStatusResponse;
import io.github.thena3ik.airalertmonitor.service.RegionQueryService;
import lombok.RequiredArgsConstructor;
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
}
