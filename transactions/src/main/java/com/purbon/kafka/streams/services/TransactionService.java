package com.purbon.kafka.streams.services;


import com.purbon.kafka.streams.model.TransactionE;
import com.purbon.kafka.streams.serdes.CustomSerdes;
import com.purbon.kafka.streams.topologies.TransactionTopologyBuilder;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class TransactionService {

    Logger logger = LoggerFactory.getLogger(TransactionService.class);

    @Value("${spring.kafka.properties.schema.registry.url}")
    private String schemaRegistryUrl;

    @Autowired
    private CustomSerdes customSerdes;

    @Bean
    public KStream<Long, TransactionE> process(@Qualifier("defaultKafkaStreamsBuilder") StreamsBuilder builder) {

        Map<String, Object> config = new HashMap<>();
        config.put("schema.registry.url", schemaRegistryUrl);

        TransactionTopologyBuilder ttb = new TransactionTopologyBuilder(customSerdes, config, 5);
        return ttb.build(builder);
    }

}
