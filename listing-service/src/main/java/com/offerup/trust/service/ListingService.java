package com.offerup.trust.service;

import com.offerup.trust.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ListingService {

    private final ListingRepository listingRepository;
    private final SellerRepository sellerRepository;
    private final ModerationRepository moderationRepository;
    private final ReputationService reputationService;
    private final FraudScorerClient fraudScorerClient;
    private final KafkaTemplate<String, ListingEvent> kafkaTemplate;

    @Value("${trust.fraud-threshold:0.65}")
    private double fraudThreshold;

    @Value("${trust.review-threshold:0.45}")
    private double reviewThreshold;

    @Value("${kafka.topics.listing-submitted}")
    private String listingSubmittedTopic;

    @Transactional
    public Listing submitListing(ListingRequest request) {
        Seller seller = sellerRepository.findByExternalId(request.getSellerId())
            .orElseThrow(() -> new RuntimeException("Seller not found: " + request.getSellerId()));

        Listing listing = new Listing();
        listing.setSeller(seller);
        listing.setTitle(request.getTitle());
        listing.setDescription(request.getDescription());
        listing.setPrice(request.getPrice());
        listing.setCategory(request.getCategory());
        listing.setCondition(request.getCondition());
        listing.setLocationCity(request.getLocationCity());
        listing.setLocationState(request.getLocationState());
        listing.setImageHash(generateSimHash(request.getTitle() + request.getDescription()));

        listing = listingRepository.save(listing);

        // Publish to Kafka for async scoring
        ListingEvent event = buildEvent(listing, seller);
        kafkaTemplate.send(listingSubmittedTopic, listing.getId().toString(), event);

        log.info("Listing {} submitted, event published to Kafka", listing.getId());
        return listing;
    }

    @Transactional
    public void scoreListingSync(ListingEvent event) {
        Optional<Listing> listingOpt = listingRepository.findById(event.getListingId());
        if (listingOpt.isEmpty()) return;

        Listing listing = listingOpt.get();
        Seller seller = listing.getSeller();

        FraudScoreResponse score = fraudScorerClient.score(event);
        double fraudProb = score.getFraudProbability();

        listing.setFraudScore(fraudProb);
        listing.setScoredAt(Instant.now());
        listing.setShapValues(score.getShapValues());

        if (fraudProb >= fraudThreshold) {
            listing.setTrustLabel(Listing.TrustLabel.FLAGGED);
            listing.setFlaggedReason(score.getTopReason());
            createModerationItem(listing, fraudProb, score.getTopReason());
        } else if (fraudProb >= reviewThreshold) {
            listing.setTrustLabel(Listing.TrustLabel.REVIEW);
        } else {
            listing.setTrustLabel(Listing.TrustLabel.CLEAN);
        }

        listingRepository.save(listing);
        reputationService.updateAfterScoredListing(seller, fraudProb);

        log.info("Listing {} scored: fraudProb={}, label={}", listing.getId(), fraudProb, listing.getTrustLabel());
    }

    public Optional<Listing> getListing(UUID id) {
        return listingRepository.findById(id);
    }

    private void createModerationItem(Listing listing, double fraudScore, String reason) {
        ModerationItem item = new ModerationItem();
        item.setListing(listing);
        item.setFraudScore(fraudScore);
        item.setFlaggedReason(reason);
        moderationRepository.save(item);
    }

    private ListingEvent buildEvent(Listing listing, Seller seller) {
        long ageDays = ChronoUnit.DAYS.between(
            seller.getAccountCreatedAt(), Instant.now()
        );
        return new ListingEvent(
            listing.getId(),
            seller.getExternalId(),
            listing.getTitle(),
            listing.getDescription(),
            listing.getPrice(),
            listing.getCategory(),
            listing.getCondition(),
            listing.getLocationCity(),
            listing.getLocationState(),
            reputationService.computeScore(seller),
            (int) ageDays,
            seller.getTotalListings(),
            seller.getDisputeCount()
        );
    }

    // SimHash stub - in production use Guava's Hashing.simHash
    private String generateSimHash(String input) {
        return Integer.toHexString(input.hashCode());
    }
}
