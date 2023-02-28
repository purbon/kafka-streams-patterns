package com.purbon.streams.ks.services;

import com.purbon.streams.ks.topologies.transformer.MessageTransformer;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;

/**
 * NOTE: This component is keep here for historic reasons and is not used in this
 * software. However, if found interesting this is an example of how to build a service
 * with an external enrichment with a simple Kafka Streams flow
 *
 * When starting the SpringBoot App this component will not be used as it has no annotation.
 */
public class EnrichmentByApiService {

    Logger logger = LoggerFactory.getLogger(EnrichmentByApiService.class);

    @Autowired
    private WebApiCallService apiService;

    private String sourceTopic;
    private String targetTopic;

    public EnrichmentByApiService(WebApiCallService apiService) {
        this.sourceTopic = "app.topic";
        this.targetTopic = "app.topic.next.2";
        this.apiService = apiService;
    }

    @Bean("app1StreamTopology")
    public KStream<String, String> process(@Qualifier("app1StreamBuilder") StreamsBuilder builder) {
        var flow = builder
                .stream(sourceTopic, Consumed.with(Serdes.String(), Serdes.String()));

                flow.peek((key, value) -> logger.debug("1:" + key + " - " + value))
                    .transform(() -> new MessageTransformer(apiService))
                    .peek((key, value) -> logger.debug("2: " + key + " - " + value))
                    .to(targetTopic);

        return flow;
    }
}
