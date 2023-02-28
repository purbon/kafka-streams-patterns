package com.purbon.kafka.streams.services;


import com.purbon.kafka.streams.config.BasicAuthConfig;
import com.purbon.kafka.streams.config.SchemaRegistryConfig;
import com.purbon.kafka.streams.topologies.WireUpTopology;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class WireUpService {

    Logger logger = LoggerFactory.getLogger(WireUpService.class);

    @Value("${app.source.topic}")
    private String sourceTopic;

    @Value("${app.target.topic}")
    private String targetTopic;

    @NonNull private SchemaRegistryConfig schemaRegistryConfig;

    @NonNull private BasicAuthConfig basicAuthConfig;

    @Bean
    public KStream<String, String> process(StreamsBuilder streamsBuilder) {
        var builder = new WireUpTopology.Builder()
                .setSourceTopic(sourceTopic)
                .setTargetTopic(targetTopic);
        var topology = builder.build(streamsBuilder);
        return topology.getStream();
    }

}
