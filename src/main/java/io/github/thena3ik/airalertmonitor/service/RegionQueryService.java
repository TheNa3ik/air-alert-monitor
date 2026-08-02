package io.github.thena3ik.airalertmonitor.service;

import io.github.thena3ik.airalertmonitor.dto.RegionStatusResponse;
import io.github.thena3ik.airalertmonitor.entity.AlertEvent;
import io.github.thena3ik.airalertmonitor.entity.Region;
import io.github.thena3ik.airalertmonitor.exception.RegionNotFoundException;
import io.github.thena3ik.airalertmonitor.repository.AlertEventRepository;
import io.github.thena3ik.airalertmonitor.repository.RegionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RegionQueryService {

    private final AlertEventRepository alertEventRepository;
    private final RegionRepository regionRepository;

    public List<RegionStatusResponse> getAllRegionStatuses(List<Long> ids, Boolean activeFilter) {
        List<Region> regions = (ids != null && !ids.isEmpty())
                ? regionRepository.findAllById(ids)
                : regionRepository.findAll();

        return regions.stream()
                .map(this::toStatusResponse)
                .filter(status -> activeFilter == null || status.alertActive() == activeFilter)
                .toList();
    }

    public RegionStatusResponse getRegionStatus(Long id) {
        Region region = regionRepository.findById(id)
                .orElseThrow(() -> new RegionNotFoundException("Region not found with id: " + id));

        return toStatusResponse(region);
    }

    private RegionStatusResponse toStatusResponse(Region region) {
        Optional<AlertEvent> openEvent = alertEventRepository.findByRegionAndEndedAtIsNull(region);

        boolean alertActive = openEvent.isPresent();
        var since = openEvent.map(AlertEvent::getStartedAt).orElse(null);

        return new RegionStatusResponse(region.getId(), region.getName(), alertActive, since);
    }
}
