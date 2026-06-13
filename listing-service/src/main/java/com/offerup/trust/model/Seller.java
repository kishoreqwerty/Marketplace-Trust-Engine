package com.offerup.trust.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "sellers")
@Data
@NoArgsConstructor
public class Seller {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true)
    private String externalId;

    private String username;
    private Instant accountCreatedAt;
    private int totalListings = 0;
    private int totalSales = 0;
    private int disputeCount = 0;
    private int reportCount = 0;
    private double reputationScore = 0.5;
    private Instant lastUpdated = Instant.now();
}
