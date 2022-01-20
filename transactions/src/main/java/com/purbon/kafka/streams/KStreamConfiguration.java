package com.purbon.kafka.streams;

import com.purbon.kafka.streams.serdes.CustomSerdes;
import com.purbon.kafka.streams.topologies.DelayedTxInfraCustomizer;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.streams.StreamsConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.autoconfigure.kafka.StreamsBuilderFactoryBeanCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration;
import org.springframework.kafka.config.KafkaStreamsConfiguration;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;

import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class KStreamConfiguration {

    @Autowired
    private KafkaProperties kafkaProperties;

    @Autowired
    private KafkaStreamsHealthIndicator healthIndicator;

    @Autowired
    private CustomSerdes serdes;

    @Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
    public KafkaStreamsConfiguration getStreamsConfig() {
        Map<String, Object> props = kafkaProperties.buildStreamsProperties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "app1");
        props.put("spring.kafka.streams.properties.state.dir","/tmp/kafka-streams/1");
        return new KafkaStreamsConfiguration(props);
    }

    @Bean
    public StreamsBuilderFactoryBeanCustomizer streamsBuilderFactoryBeanCustomizer() {
       return factoryBean -> {

           Map<String, Object> serdeConfig = new HashMap<>();
           serdeConfig.put("schema.registry.url", "http://localhost:8081");

           factoryBean.setStateListener(healthIndicator);
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

    @Bean(name = "delayedConfigTopology")
    public KafkaStreamsConfiguration delayedTopologyConfig() {
        Map<String, Object> props = kafkaProperties.buildStreamsProperties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "app2");
        props.put("spring.kafka.streams.properties.state.dir","/tmp/kafka-streams/2");
        return new KafkaStreamsConfiguration(props);
    }

  @Bean(name = "delayedConfigTopologyConfig")
    public StreamsBuilderFactoryBean streamsBuilderFactoryBean(KafkaStreamsConfiguration delayedConfigTopology) throws Exception {

        Map<String, Object> serdeConfig = new HashMap<>();
        serdeConfig.put("schema.registry.url", "http://localhost:8081");

        StreamsBuilderFactoryBean streamsBuilderFactoryBean =
                new StreamsBuilderFactoryBean(delayedConfigTopology);
        streamsBuilderFactoryBean.afterPropertiesSet();
        streamsBuilderFactoryBean.setInfrastructureCustomizer(new DelayedTxInfraCustomizer(serdes, serdeConfig));
        streamsBuilderFactoryBean.setCloseTimeout(10); //10 seconds
        return streamsBuilderFactoryBean;
    }
}
