package com.purbon.kafka.streams.services;


import com.purbon.kafka.streams.topologies.OltpTopicNameExtractor;
import com.purbon.kafka.streams.topologies.RoutingTopologyBuilder;
import lombok.RequiredArgsConstructor;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

@Configuration
@RequiredArgsConstructor
public class RoutingService {

    Logger logger = LoggerFactory.getLogger(RoutingService.class);

    @Value("${message.router.topic.name.regex}")
    private String appTopicRegex;

    @Value("${spring.kafka.properties.schema.registry.url}")
    private String schemaRegistryUrl;

    @Value("${spring.kafka.properties.schema.registry.ssl.keystore.password}")
    private String schemaRegistryKeystorePassword;

    @Value("${spring.kafka.properties.schema.registry.ssl.keystore.location}")
    private String schemaRegistryKeystoreLocation;

    @Value("${spring.kafka.properties.schema.registry.ssl.truststore.password}")
    private String schemaRegistryTrustStorePassword;

    @Value("${spring.kafka.properties.schema.registry.ssl.truststore.location}")
    private String schemaRegistryTrustStoreLocation;

    @Value("${spring.kafka.properties.schema.registry.ssl.key.password}")
    private String schemaRegistrySslKeyPassword;

    @Value("${spring.kafka.properties.basic.auth.credentials.source}")
    private String schemaRegistryAuthCredentials;

    @Value("${spring.kafka.properties.basic.auth.user.info}")
    private String schemaRegistryAuthUserInfo;

    @Value("${message.router.topic.name.replacement}")
    private String topicNameReplacement;

    @Bean
    public KStream<GenericRecord, GenericRecord> process(StreamsBuilder builder) {
        OltpTopicNameExtractor topicNameExtractor = new OltpTopicNameExtractor(appTopicRegex, topicNameReplacement);

        var schemaRegistryConfig = new HashMap<String, String>();
        schemaRegistryConfig.put("schema.registry.url", schemaRegistryUrl);
        if (schemaRegistryUrl.startsWith("https://")) {
            schemaRegistryConfig.put("schema.registry.ssl.keystore.password", schemaRegistryKeystorePassword);
            schemaRegistryConfig.put("schema.registry.ssl.keystore.location", schemaRegistryKeystoreLocation);
            schemaRegistryConfig.put("schema.registry.ssl.truststore.password", schemaRegistryTrustStorePassword);
            schemaRegistryConfig.put("schema.registry.ssl.truststore.location", schemaRegistryTrustStoreLocation);
            schemaRegistryConfig.put("schema.registry.ssl.key.password", schemaRegistrySslKeyPassword);
        }
        if (!schemaRegistryAuthCredentials.isBlank() && !schemaRegistryAuthUserInfo.isBlank()) {
            schemaRegistryConfig.put("basic.auth.credentials.source", schemaRegistryAuthCredentials);
            schemaRegistryConfig.put("basic.auth.user.info", schemaRegistryAuthUserInfo);
        }
        RoutingTopologyBuilder topologyBuilder = new RoutingTopologyBuilder(appTopicRegex, topicNameExtractor, schemaRegistryConfig);
        return topologyBuilder.build(builder);
    }

}
