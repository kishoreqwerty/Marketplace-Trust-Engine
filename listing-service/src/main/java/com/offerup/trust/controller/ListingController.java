package com.offerup.trust.controller;

import com.offerup.trust.model.Listing;
import com.offerup.trust.model.ListingRequest;
import com.offerup.trust.service.AiListingAssistant;
import com.offerup.trust.service.ListingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/listings")
@RequiredArgsConstructor
public class ListingController {

    private final ListingService listingService;
    private final AiListingAssistant aiListingAssistant;

    @PostMapping
    public ResponseEntity<Listing> submitListing(@RequestBody ListingRequest request) {
        Listing listing = listingService.submitListing(request);
        return ResponseEntity.accepted().body(listing);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Listing> getListing(@PathVariable UUID id) {
        return listingService.getListing(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/ai-improve")
    public ResponseEntity<Map<String, Object>> aiImprove(
        @PathVariable UUID id,
        @RequestBody ListingRequest request
    ) {
        Map<String, Object> suggestions = aiListingAssistant.improveListing(request);
        return ResponseEntity.ok(suggestions);
    }
}
