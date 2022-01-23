package com.purbon.kafka.streams.services;


import com.purbon.kafka.streams.config.BasicAuthConfig;
import com.purbon.kafka.streams.config.FilterConfig;
import com.purbon.kafka.streams.config.SchemaRegistryConfig;
import com.purbon.kafka.streams.topologies.FilterTopology;
import com.purbon.kafka.streams.topologies.OltpTopicNameExtractor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.regex.Pattern;

@Configuration
@RequiredArgsConstructor
public class FilteringService {

    Logger logger = LoggerFactory.getLogger(FilteringService.class);

    @Value("${message.router.topic.name.regex}")
    private String appTopicRegex;

    @Value("${message.router.topic.name.replacement}")
    private String topicNameReplacement;

    @Value("${message.filter.config.table.name}")
    private String configTableName;

    @NonNull private SchemaRegistryConfig schemaRegistryConfig;
    @NonNull private BasicAuthConfig basicAuthConfig;
    @NonNull private FilterConfig filterConfig;


    @Bean
    public KStream<GenericRecord, GenericRecord> process(StreamsBuilder streamsBuilder) {
        OltpTopicNameExtractor topicNameExtractor = new OltpTopicNameExtractor(appTopicRegex, topicNameReplacement);

        var configMap = schemaRegistryConfig.asMap();
        if (!basicAuthConfig.isEmpty()) {
            configMap.put("basic.auth.credentials.source", basicAuthConfig.getCredentials_source());
            configMap.put("basic.auth.user.info", basicAuthConfig.getUser_info());
        }

        Map<String, String> filterMapConfig = filterConfig.asMap();

        var topology = new FilterTopology.Builder()
                .setSerdeConfig(configMap)
                .setExtractor(topicNameExtractor)
                .setTopicPattern(Pattern.compile(appTopicRegex))
                .setConfigurationTableName(configTableName)
                .setFilterConfig(filterMapConfig)
                .build(streamsBuilder);

        return topology.getStream();
    }

}
