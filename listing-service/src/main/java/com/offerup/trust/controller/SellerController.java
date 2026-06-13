package com.offerup.trust.controller;

import com.offerup.trust.model.Seller;
import com.offerup.trust.model.SellerRequest;
import com.offerup.trust.service.ReputationService;
import com.offerup.trust.service.SellerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/sellers")
@RequiredArgsConstructor
public class SellerController {

    private final SellerRepository sellerRepository;
    private final ReputationService reputationService;

    @PostMapping
    public ResponseEntity<Seller> createSeller(@RequestBody SellerRequest request) {
        Seller seller = new Seller();
        seller.setExternalId(request.getExternalId());
        seller.setUsername(request.getUsername());
        seller.setAccountCreatedAt(request.getAccountCreatedAt());
        return ResponseEntity.ok(sellerRepository.save(seller));
    }

    @GetMapping("/{sellerId}/reputation")
    public ResponseEntity<Map<String, Object>> getReputation(@PathVariable String sellerId) {
        double score = reputationService.getReputationScore(sellerId);
        return ResponseEntity.ok(Map.of(
            "sellerId", sellerId,
            "reputationScore", score,
            "tier", score > 0.7 ? "TRUSTED" : score > 0.4 ? "STANDARD" : "AT_RISK"
        ));
    }
}
