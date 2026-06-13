package com.offerup.trust.controller;

import com.offerup.trust.model.ModerationItem;
import com.offerup.trust.service.ModerationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/moderation")
@RequiredArgsConstructor
public class ModerationController {

    private final ModerationRepository moderationRepository;

    @GetMapping("/queue")
    public ResponseEntity<List<ModerationItem>> getQueue() {
        return ResponseEntity.ok(
            moderationRepository.findByStatusOrderByFraudScoreDesc(ModerationItem.Status.PENDING)
        );
    }

    @PostMapping("/{itemId}/approve")
    public ResponseEntity<ModerationItem> approve(@PathVariable UUID itemId) {
        return moderationRepository.findById(itemId).map(item -> {
            item.setStatus(ModerationItem.Status.APPROVED);
            item.setReviewedAt(Instant.now());
            return ResponseEntity.ok(moderationRepository.save(item));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{itemId}/reject")
    public ResponseEntity<ModerationItem> reject(@PathVariable UUID itemId) {
        return moderationRepository.findById(itemId).map(item -> {
            item.setStatus(ModerationItem.Status.REJECTED);
            item.setReviewedAt(Instant.now());
            return ResponseEntity.ok(moderationRepository.save(item));
        }).orElse(ResponseEntity.notFound().build());
    }
}
