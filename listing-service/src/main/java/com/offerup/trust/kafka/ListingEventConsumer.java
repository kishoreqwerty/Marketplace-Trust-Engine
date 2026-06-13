package com.offerup.trust.kafka;

import com.offerup.trust.model.ListingEvent;
import com.offerup.trust.service.ListingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ListingEventConsumer {

    private final ListingService listingService;

    @KafkaListener(
        topics = "${kafka.topics.listing-submitted}",
        groupId = "trust-engine-scorer"
    )
    public void consumeListingSubmitted(
        @Payload ListingEvent event,
        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
        @Header(KafkaHeaders.OFFSET) long offset
    ) {
        log.info("Received listing event: listingId={}, partition={}, offset={}",
            event.getListingId(), partition, offset);
        try {
            listingService.scoreListingSync(event);
        } catch (Exception e) {
            log.error("Failed to score listing {}: {}", event.getListingId(), e.getMessage(), e);
        }
    }
}
