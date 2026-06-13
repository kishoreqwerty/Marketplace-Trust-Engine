package com.offerup.trust.model;

import lombok.Data;
import java.time.Instant;

@Data
public class SellerRequest {
    private String externalId;
    private String username;
    private Instant accountCreatedAt;
}
