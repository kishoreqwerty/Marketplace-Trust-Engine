package com.offerup.trust.service;

import com.offerup.trust.model.FraudScoreResponse;
import com.offerup.trust.model.ListingEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class FraudScorerClient {

    private final RestTemplate restTemplate;

    @Value("${fraud-scorer.url}")
    private String fraudScorerUrl;

    public FraudScoreResponse score(ListingEvent event) {
        try {
            return restTemplate.postForObject(
                fraudScorerUrl + "/score",
                event,
                FraudScoreResponse.class
            );
        } catch (Exception e) {
            log.error("Fraud scorer unavailable, defaulting to 0.5", e);
            FraudScoreResponse fallback = new FraudScoreResponse();
            fallback.setFraudProbability(0.5);
            fallback.setLabel("UNKNOWN");
            return fallback;
        }
    }
}
