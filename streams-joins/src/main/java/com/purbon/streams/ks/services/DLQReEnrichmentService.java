package com.purbon.streams.ks.services;

import com.purbon.streams.ks.model.envelopes.MessageImpl;
import com.purbon.streams.ks.topologies.DLQBasedEnrichmentTopologyBuilder;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.stereotype.Component;

@EnableKafka
@Component
public class DLQReEnrichmentService {

    Logger logger = LoggerFactory.getLogger(DLQReEnrichmentService.class);

    @Value("${app.streaming.target.topic}")
    private String targetTopic;

    @Value("${app.streaming.enrichment.table}")
    private String kTableTopic;

    @Value("${app.streaming.dlq.topic}")
    private String dlqTopic;

    @Value("${app.streaming.parking.topic}")
    private String parkingTopic;

    @Value("${app.streaming.dlq.backoff.time.sec}")
    private long backoffTimeInSec;

    public DLQReEnrichmentService() {
    }

    @Bean("dlqAppStreamTopology")
    public KStream<String, MessageImpl<String>> process(@Qualifier("dlqAppStreamBuilder") StreamsBuilder builder) {
        DLQBasedEnrichmentTopologyBuilder topology
                = new DLQBasedEnrichmentTopologyBuilder(kTableTopic, parkingTopic, dlqTopic, targetTopic);
        topology.configureBackOffTimeInSec(backoffTimeInSec);
        return topology.build(builder);
    }

}
