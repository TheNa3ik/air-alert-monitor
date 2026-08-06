package io.github.thena3ik.airalertmonitor.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "webhook_subscription")
@Getter
@Setter
@NoArgsConstructor
public class WebhookSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 2048)
    private String url;

    @ManyToMany
    @JoinTable(
            name = "webhook_subscription_region",
            joinColumns = @JoinColumn(name = "webhook_subscription_id"),
            inverseJoinColumns = @JoinColumn(name = "region_id")
    )
    private Set<Region> regions = new HashSet<>();

    @Column(nullable = false, unique = true)
    private String secret;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "consecutive_failures", nullable = false)
    private int consecutiveFailures = 0;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public WebhookSubscription(String url, String secret) {
        this.url = url;
        this.secret = secret;
        this.createdAt = Instant.now();
    }
}