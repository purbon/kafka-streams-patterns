package com.purbon.kafka.streams.services;


import lombok.RequiredArgsConstructor;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class RoutingService {

    Logger logger = LoggerFactory.getLogger(RoutingService.class);

    @Value("${message.router.topic.name.regex}")
    private String appTopicRegex;

    @Value("${spring.kafka.properties.schema.registry.url}")
    private String schemaRegistryUrl;


    @Bean
    public KStream<GenericRecord, GenericRecord> process(StreamsBuilder builder) {
        return null;
    }

}
