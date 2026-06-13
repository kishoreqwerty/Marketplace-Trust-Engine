package com.offerup.trust.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    public static final String LISTINGS_SUBMITTED = "listings.submitted";
    public static final String LISTINGS_SCORED = "listings.scored";
    public static final String LISTINGS_FLAGGED = "listings.flagged";

    @Bean
    public NewTopic listingsSubmittedTopic() {
        return TopicBuilder.name(LISTINGS_SUBMITTED)
                .partitions(6)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic listingsScoredTopic() {
        return TopicBuilder.name(LISTINGS_SCORED)
                .partitions(6)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic listingsFlaggedTopic() {
        return TopicBuilder.name(LISTINGS_FLAGGED)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
