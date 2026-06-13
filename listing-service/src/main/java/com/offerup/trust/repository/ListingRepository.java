package com.offerup.trust.repository;

import com.offerup.trust.model.Listing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface ListingRepository extends JpaRepository<Listing, UUID> {

    Page<Listing> findByStatus(Listing.ListingStatus status, Pageable pageable);

    List<Listing> findBySellerId(UUID sellerId);

    @Query("SELECT l FROM Listing l WHERE l.fraudScore >= :threshold ORDER BY l.fraudScore DESC")
    Page<Listing> findHighRiskListings(BigDecimal threshold, Pageable pageable);

    @Query("SELECT l FROM Listing l WHERE l.status = 'FLAGGED' ORDER BY l.scoredAt DESC")
    Page<Listing> findFlaggedListings(Pageable pageable);

    long countByStatus(Listing.ListingStatus status);

    @Query("SELECT AVG(l.fraudScore) FROM Listing l WHERE l.fraudScore IS NOT NULL")
    BigDecimal getAverageFraudScore();
}
