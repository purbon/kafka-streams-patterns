package com.purbon.kafka.streams;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.AdminClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.autoconfigure.kafka.StreamsBuilderFactoryBeanCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration;
import org.springframework.kafka.config.KafkaStreamsConfiguration;

import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class KStreamConfiguration {

    @Autowired
    private KafkaProperties kafkaProperties;

    @Autowired
    private KafkaStreamsHealthIndicator healthIndicator;

    @Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
    public KafkaStreamsConfiguration getStreamsConfig() {
        Map<String, Object> props = kafkaProperties.buildStreamsProperties();
        return new KafkaStreamsConfiguration(props);
    }

    @Bean
    public StreamsBuilderFactoryBeanCustomizer streamsBuilderFactoryBeanCustomizer() {
       return factoryBean -> {
           factoryBean.setStateListener(healthIndicator);
           //factoryBean.setInfrastructureCustomizer(new MappingTopologyCustomizer());
           factoryBean.setUncaughtExceptionHandler((t, e) -> {
              if (e != null) {
                  healthIndicator.setCurrentException(e);
              }
           });
       };
    }

    @Bean(name = "healthAdminClient")
    public AdminClient admin() {
        Map<String, Object> configs = kafkaProperties.buildStreamsProperties();
        configs.put("request.timeout.ms", 10000);
        configs.put("default.api.timeout.ms", 10000);
        configs.put("retries", 4);
        return AdminClient.create(configs);
    }

}
