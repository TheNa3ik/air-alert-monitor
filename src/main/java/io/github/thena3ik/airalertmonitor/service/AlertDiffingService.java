package io.github.thena3ik.airalertmonitor.service;

import io.github.thena3ik.airalertmonitor.dto.UbillingAlertsResponse;
import io.github.thena3ik.airalertmonitor.entity.AlertEvent;
import io.github.thena3ik.airalertmonitor.entity.Region;
import io.github.thena3ik.airalertmonitor.repository.AlertEventRepository;
import io.github.thena3ik.airalertmonitor.repository.RegionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertDiffingService {

    private final RegionRepository regionRepository;
    private final AlertEventRepository alertEventRepository;

    @Transactional
    public void processPoll(UbillingAlertsResponse response) {
        for (var entry : response.states().entrySet()) {
            String regionName = entry.getKey();
            boolean alertNowActive = entry.getValue().alertnow();

            Optional<Region> maybeRegion = regionRepository.findByName(regionName);

            if (maybeRegion.isEmpty()) {
                log.warn("Unknown region from API: {}, skipping", regionName);
                continue;
            }
            Region region = maybeRegion.get();

            Optional<AlertEvent> openEvent = alertEventRepository.findByRegionAndEndedAtIsNull(region);

            if (openEvent.isEmpty() && alertNowActive) {
                alertEventRepository.save(new AlertEvent(region, Instant.now(), response.source()));
            } else if (openEvent.isPresent() && !alertNowActive) {
                AlertEvent eventToClose = openEvent.get();
                eventToClose.close(Instant.now());
                alertEventRepository.save(eventToClose);
            }
        }
    }
}
