package com.offerup.trust.model;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ListingRequest {
    private String sellerId;
    private String title;
    private String description;
    private BigDecimal price;
    private String category;
    private String condition;
    private String locationCity;
    private String locationState;
    private String imageUrl;
}
