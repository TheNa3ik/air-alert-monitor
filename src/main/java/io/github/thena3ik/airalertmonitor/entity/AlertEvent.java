package io.github.thena3ik.airalertmonitor.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "alert_event")
public class AlertEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt = Instant.now();

    @Column(name = "ended_at")
    private Instant endedAt;

    @Column(nullable = false, length = 100)
    private String source;

    public AlertEvent(Region region, Instant startedAt, String source) {
        this.region = region;
        this.startedAt = startedAt;
        this.source = source;
    }

    public boolean isOngoing() {
        return endedAt == null;
    }

    public void close(Instant endedAt) {
        this.endedAt = endedAt;
    }
}
