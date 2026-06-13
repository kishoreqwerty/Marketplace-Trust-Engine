package com.offerup.trust.service;

import com.offerup.trust.model.Seller;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReputationService {

    private final RedisTemplate<String, Double> redisTemplate;
    private final SellerRepository sellerRepository;

    @Value("${trust.reputation.ttl-seconds:3600}")
    private long ttlSeconds;

    private static final String KEY_PREFIX = "seller:reputation:";

    public double getReputationScore(String externalSellerId) {
        String key = KEY_PREFIX + externalSellerId;
        Double cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            return cached;
        }

        Optional<Seller> sellerOpt = sellerRepository.findByExternalId(externalSellerId);
        double score = sellerOpt.map(this::computeScore).orElse(0.5);

        redisTemplate.opsForValue().set(key, score, Duration.ofSeconds(ttlSeconds));
        return score;
    }

    public void invalidateCache(String externalSellerId) {
        redisTemplate.delete(KEY_PREFIX + externalSellerId);
    }

    public double computeScore(Seller seller) {
        double score = 0.5;

        // Account age factor (up to +0.2)
        long ageDays = java.time.temporal.ChronoUnit.DAYS.between(
            seller.getAccountCreatedAt().atOffset(java.time.ZoneOffset.UTC).toLocalDate(),
            java.time.LocalDate.now()
        );
        score += Math.min(0.2, ageDays / 365.0 * 0.2);

        // Sales history factor (up to +0.2)
        if (seller.getTotalListings() > 0) {
            double salesRatio = (double) seller.getTotalSales() / seller.getTotalListings();
            score += salesRatio * 0.2;
        }

        // Dispute penalty
        if (seller.getTotalListings() > 0) {
            double disputeRate = (double) seller.getDisputeCount() / seller.getTotalListings();
            score -= disputeRate * 0.3;
        }

        // Report penalty
        score -= Math.min(0.3, seller.getReportCount() * 0.05);

        return Math.max(0.0, Math.min(1.0, score));
    }

    public void updateAfterScoredListing(Seller seller, double fraudScore) {
        seller.setTotalListings(seller.getTotalListings() + 1);
        if (fraudScore > 0.65) {
            seller.setReportCount(seller.getReportCount() + 1);
        }
        seller.setReputationScore(computeScore(seller));
        seller.setLastUpdated(java.time.Instant.now());
        sellerRepository.save(seller);
        invalidateCache(seller.getExternalId());
    }
}
