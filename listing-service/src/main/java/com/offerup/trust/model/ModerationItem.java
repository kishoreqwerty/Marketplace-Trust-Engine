package com.offerup.trust.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "moderation_queue")
@Data
@NoArgsConstructor
public class ModerationItem {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id")
    private Listing listing;

    private double fraudScore;
    private String flaggedReason;

    @Enumerated(EnumType.STRING)
    private Status status = Status.PENDING;

    private String reviewedBy;
    private Instant reviewedAt;
    private Instant createdAt = Instant.now();

    public enum Status { PENDING, APPROVED, REJECTED }
}
