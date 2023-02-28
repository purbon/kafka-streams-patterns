package com.purbon.streams.ks;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration;
import org.springframework.kafka.config.KafkaStreamsConfiguration;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;

import java.util.Map;

@Configuration
public class KStreamConfiguration {

    @Autowired
    private KafkaProperties kafkaProperties;

    @Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
    public StreamsConfig kStreamsConfigs() {
        Map<String, Object> config = setDefaults();
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "1");
        return new StreamsConfig(config);
    }

    public Map<String, Object> setDefaults() {
        return kafkaProperties.buildStreamsProperties();
    }

    @Bean("app1StreamBuilder")
    public StreamsBuilderFactoryBean app1StreamBuilderFactoryBean() {
        Map<String, Object> config = setDefaults();
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "app1");
        config.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 3000);
        return new StreamsBuilderFactoryBean(new KafkaStreamsConfiguration(config));
    }

    @Bean("dlqAppStreamBuilder")
    public StreamsBuilderFactoryBean dlqAppStreamBuilderFactoryBean() {
        Map<String, Object> config = setDefaults();
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "app1.dlq");
        config.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 3000);

        StoreBuilder dlqStoreBuilder =
                Stores.keyValueStoreBuilder(
                        Stores.persistentKeyValueStore("dlq.store"),
                        Serdes.String(),
                        Serdes.String()
                );

        var factory = new StreamsBuilderFactoryBean(new KafkaStreamsConfiguration(config));
        factory.getTopology()
                .addStateStore(dlqStoreBuilder, "DelayReadProcessor");
        return factory;
    }
}
