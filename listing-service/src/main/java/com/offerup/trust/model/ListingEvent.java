package com.offerup.trust.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ListingEvent {
    private UUID listingId;
    private String sellerId;
    private String title;
    private String description;
    private BigDecimal price;
    private String category;
    private String condition;
    private String locationCity;
    private String locationState;
    private double sellerReputationScore;
    private int sellerAccountAgeDays;
    private int sellerTotalListings;
    private int sellerDisputeCount;
}
