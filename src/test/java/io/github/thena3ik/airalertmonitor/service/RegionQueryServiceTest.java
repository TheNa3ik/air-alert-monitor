package io.github.thena3ik.airalertmonitor.service;

import io.github.thena3ik.airalertmonitor.dto.RegionAlertStatsResponse;
import io.github.thena3ik.airalertmonitor.entity.AlertEvent;
import io.github.thena3ik.airalertmonitor.entity.Region;
import io.github.thena3ik.airalertmonitor.exception.InvalidDateRangeException;
import io.github.thena3ik.airalertmonitor.exception.InvalidPeriodException;
import io.github.thena3ik.airalertmonitor.exception.InvalidTimezoneException;
import io.github.thena3ik.airalertmonitor.repository.AlertEventRepository;
import io.github.thena3ik.airalertmonitor.repository.RegionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegionQueryServiceTest {

    @Mock
    private RegionRepository regionRepository;

    @Mock
    private AlertEventRepository alertEventRepository;

    @InjectMocks
    private RegionQueryService regionQueryService;

    @Test
    void includesOngoingEvent_usingNowAsEndTime() {
        Region region = new Region(1L, "Test Region");
        Instant startedAt = Instant.now().minus(10, ChronoUnit.MINUTES);
        AlertEvent ongoing = new AlertEvent(region, startedAt, "test-source");

        when(regionRepository.findById(1L)).thenReturn(Optional.of(region));
        when(alertEventRepository.findByRegionAndStartedAtBetween(any(), any(), any()))
                .thenReturn(List.of(ongoing));

        RegionAlertStatsResponse stats = regionQueryService.getAlertRegionStats(1L, "day", null, null, "UTC");

        assertThat(stats.totalAlertSeconds()).isGreaterThanOrEqualTo(590);
        assertThat(stats.eventCount()).isEqualTo(1);
    }

    @Test
    void throwsInvalidPeriodException_forUnknownPeriod() {
        Region region = new Region(1L, "Test Region");
        when(regionRepository.findById(1L)).thenReturn(Optional.of(region));

        assertThatThrownBy(() -> regionQueryService.getAlertRegionStats(1L, "fortnight", null, null, "UTC"))
                .isInstanceOf(InvalidPeriodException.class);
    }

    @Test
    void throwsInvalidDateRangeException_whenFromIsAfterTo() {
        Region region = new Region(1L, "Test Region");
        when(regionRepository.findById(1L)).thenReturn(Optional.of(region));

        LocalDateTime from = LocalDateTime.now();
        LocalDateTime to = from.minusDays(1);

        assertThatThrownBy(() -> regionQueryService.getAlertRegionStats(1L, null, from, to, "UTC"))
                .isInstanceOf(InvalidDateRangeException.class);
    }

    @Test
    void throwsInvalidTimezoneException_forBadTimezoneString() {
        Region region = new Region(1L, "Test Region");
        when(regionRepository.findById(1L)).thenReturn(Optional.of(region));

        assertThatThrownBy(() -> regionQueryService.getAlertRegionStats(1L, "day", null, null, "Not/AZone"))
                .isInstanceOf(InvalidTimezoneException.class);
    }
}