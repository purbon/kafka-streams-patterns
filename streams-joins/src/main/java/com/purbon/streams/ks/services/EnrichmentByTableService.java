package com.purbon.streams.ks.services;

import com.purbon.streams.ks.model.envelopes.MessageImpl;
import com.purbon.streams.ks.topologies.EnrichmentTopologyBuilder;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.stereotype.Component;

@EnableKafka
@Component
public class EnrichmentByTableService {

    Logger logger = LoggerFactory.getLogger(EnrichmentByTableService.class);

    @Value("${app.streaming.source.topic}")
    private String sourceTopic;

    @Value("${app.streaming.target.topic}")
    private String targetTopic;

    @Value("${app.streaming.enrichment.table}")
    private String kTableTopic;

    @Value("${app.streaming.dlq.topic}")
    private String dlqTopic;

    public EnrichmentByTableService() {
    }

    @Bean("app1StreamTopology")
    public KStream<String, MessageImpl<String>> process(@Qualifier("app1StreamBuilder") StreamsBuilder builder) {
        EnrichmentTopologyBuilder topology
                = new EnrichmentTopologyBuilder(kTableTopic, dlqTopic, sourceTopic, targetTopic);
        return topology.build(builder);
    }

}
