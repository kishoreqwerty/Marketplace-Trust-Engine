package com.offerup.trust.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "listings")
@Data
@NoArgsConstructor
public class Listing {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id")
    private Seller seller;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private BigDecimal price;
    private String category;
    private String condition;
    private String locationCity;
    private String locationState;
    private String imageHash;

    private Double fraudScore;

    @Enumerated(EnumType.STRING)
    private TrustLabel trustLabel = TrustLabel.PENDING;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Double> shapValues;

    private String flaggedReason;

    private Instant createdAt = Instant.now();
    private Instant scoredAt;

    public enum TrustLabel {
        PENDING, CLEAN, REVIEW, FLAGGED
    }
}
