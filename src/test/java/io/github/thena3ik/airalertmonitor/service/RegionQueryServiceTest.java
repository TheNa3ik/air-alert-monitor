package io.github.thena3ik.airalertmonitor.service;

import io.github.thena3ik.airalertmonitor.dto.region.RegionAlertStatsResponse;
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
import org.springframework.data.jpa.domain.PredicateSpecification;

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
        Region region = new Region(1L, "Test Region", "Test Region EN");
        Instant startedAt = Instant.now().minus(10, ChronoUnit.MINUTES);
        AlertEvent ongoing = new AlertEvent(region, startedAt, "test-source");

        when(regionRepository.findById(1L)).thenReturn(Optional.of(region));
        when(alertEventRepository.findAll(any(PredicateSpecification.class)))
                .thenReturn(List.of(ongoing));

        RegionAlertStatsResponse statistics = regionQueryService.getRegionStatistics(1L, "day", null, null, "UTC", "ua");

        assertThat(statistics.totalAlertSeconds()).isGreaterThanOrEqualTo(590);
        assertThat(statistics.eventCount()).isEqualTo(1);
    }

    @Test
    void throwsInvalidPeriodException_forUnknownPeriod() {
        Region region = new Region(1L, "Test Region", "Test Region EN");
        when(regionRepository.findById(1L)).thenReturn(Optional.of(region));

        assertThatThrownBy(() -> regionQueryService.getRegionStatistics(1L, "fortnight", null, null, "UTC", "ua"))
                .isInstanceOf(InvalidPeriodException.class);
    }

    @Test
    void throwsInvalidDateRangeException_whenFromIsAfterTo() {
        Region region = new Region(1L, "Test Region", "Test Region EN");
        when(regionRepository.findById(1L)).thenReturn(Optional.of(region));

        LocalDateTime fromDate = LocalDateTime.now();
        LocalDateTime toDate = fromDate.minusDays(1);

        assertThatThrownBy(() -> regionQueryService.getRegionStatistics(1L, null, fromDate, toDate, "UTC", "ua"))
                .isInstanceOf(InvalidDateRangeException.class);
    }

    @Test
    void throwsInvalidTimezoneException_forBadTimezoneString() {
        Region region = new Region(1L, "Test Region", "Test Region EN");
        when(regionRepository.findById(1L)).thenReturn(Optional.of(region));

        assertThatThrownBy(() -> regionQueryService.getRegionStatistics(1L, "day", null, null, "Not/AZone", "ua"))
                .isInstanceOf(InvalidTimezoneException.class);
    }
}