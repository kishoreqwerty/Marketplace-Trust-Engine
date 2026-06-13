package com.offerup.trust.service;

import com.offerup.trust.model.Seller;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReputationServiceTest {

    @Mock RedisTemplate<String, Double> redisTemplate;
    @Mock SellerRepository sellerRepository;
    @Mock ValueOperations<String, Double> valueOps;

    @InjectMocks
    ReputationService reputationService;

    @Test
    void newSeller_shouldHaveBaseScore() {
        Seller seller = new Seller();
        seller.setAccountCreatedAt(Instant.now().minus(30, ChronoUnit.DAYS));
        seller.setTotalListings(0);
        seller.setDisputeCount(0);
        seller.setReportCount(0);

        double score = reputationService.computeScore(seller);

        assertThat(score).isBetween(0.5, 0.6);
    }

    @Test
    void sellerWithDisputes_shouldHaveLowerScore() {
        Seller clean = new Seller();
        clean.setAccountCreatedAt(Instant.now().minus(365, ChronoUnit.DAYS));
        clean.setTotalListings(20);
        clean.setTotalSales(18);
        clean.setDisputeCount(0);
        clean.setReportCount(0);

        Seller disputed = new Seller();
        disputed.setAccountCreatedAt(Instant.now().minus(365, ChronoUnit.DAYS));
        disputed.setTotalListings(20);
        disputed.setTotalSales(10);
        disputed.setDisputeCount(8);
        disputed.setReportCount(3);

        double cleanScore = reputationService.computeScore(clean);
        double disputedScore = reputationService.computeScore(disputed);

        assertThat(cleanScore).isGreaterThan(disputedScore);
    }

    @Test
    void score_shouldNeverExceedBounds() {
        Seller extremeGood = new Seller();
        extremeGood.setAccountCreatedAt(Instant.now().minus(3650, ChronoUnit.DAYS));
        extremeGood.setTotalListings(1000);
        extremeGood.setTotalSales(999);
        extremeGood.setDisputeCount(0);
        extremeGood.setReportCount(0);

        Seller extremeBad = new Seller();
        extremeBad.setAccountCreatedAt(Instant.now());
        extremeBad.setTotalListings(10);
        extremeBad.setTotalSales(0);
        extremeBad.setDisputeCount(10);
        extremeBad.setReportCount(20);

        assertThat(reputationService.computeScore(extremeGood)).isLessThanOrEqualTo(1.0);
        assertThat(reputationService.computeScore(extremeBad)).isGreaterThanOrEqualTo(0.0);
    }
}
