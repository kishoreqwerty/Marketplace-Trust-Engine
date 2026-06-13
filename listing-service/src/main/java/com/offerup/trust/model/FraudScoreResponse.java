package com.offerup.trust.model;

import lombok.Data;
import java.util.Map;

@Data
public class FraudScoreResponse {
    private double fraudProbability;
    private String label;
    private Map<String, Double> shapValues;
    private String topReason;
}
