package com.offerup.trust.service;

import com.offerup.trust.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListingServiceTest {

    @Mock ListingRepository listingRepository;
    @Mock SellerRepository sellerRepository;
    @Mock ModerationRepository moderationRepository;
    @Mock ReputationService reputationService;
    @Mock FraudScorerClient fraudScorerClient;
    @Mock KafkaTemplate<String, ListingEvent> kafkaTemplate;

    @InjectMocks
    ListingService listingService;

    @Test
    void submitListing_withUnknownSeller_shouldThrow() {
        when(sellerRepository.findByExternalId("unknown")).thenReturn(Optional.empty());

        ListingRequest req = new ListingRequest();
        req.setSellerId("unknown");

        assertThatThrownBy(() -> listingService.submitListing(req))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Seller not found");
    }

    @Test
    void submitListing_shouldPublishKafkaEvent() {
        Seller seller = new Seller();
        seller.setExternalId("seller_1");
        seller.setAccountCreatedAt(Instant.now().minusSeconds(86400));

        when(sellerRepository.findByExternalId("seller_1")).thenReturn(Optional.of(seller));
        when(listingRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(reputationService.computeScore(seller)).thenReturn(0.7);
        when(kafkaTemplate.send(any(), any(), any())).thenReturn(null);

        ListingRequest req = new ListingRequest();
        req.setSellerId("seller_1");
        req.setTitle("iPhone 14");
        req.setPrice(BigDecimal.valueOf(600));
        req.setCategory("electronics");

        listingService.submitListing(req);

        verify(kafkaTemplate).send(any(), any(), any());
    }
}
