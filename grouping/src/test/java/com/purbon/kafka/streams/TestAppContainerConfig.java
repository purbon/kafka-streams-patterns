package com.purbon.kafka.streams;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.utility.DockerImageName;


@TestConfiguration
public class TestAppContainerConfig implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private KafkaContainer kafkaContainer;
    private SchemaRegistryContainer schemaRegistryContainer;

    private static final DockerImageName DEFAULT_IMAGE_NAME = DockerImageName.parse("confluentinc/cp-kafka");

    TestAppContainerConfig() {
        kafkaContainer = new KafkaContainer(DEFAULT_IMAGE_NAME.withTag("6.2.0"))
                .withNetworkAliases("kafka")
                .withNetwork(Network.newNetwork());
        schemaRegistryContainer = new SchemaRegistryContainer(kafkaContainer);
        kafkaContainer.start();
        schemaRegistryContainer.start();
    }

    @Bean
    public KafkaContainer kafka() {
        return kafkaContainer;
    }

    @Bean
    public SchemaRegistryContainer schemaRegistry() {
        return schemaRegistryContainer;
    }

    @Override
    public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
        TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
                configurableApplicationContext, "spring.kafka.bootstrap-servers=" + kafkaContainer.getBootstrapServers(),
                "spring.kafka.properties.schema.registry.url="+schemaRegistryContainer.getUrl());
    }

    @Bean
    public NewTopic avroTopic() {
        return new NewTopic("oltp.tableA.avro", 1, (short) 1);
    }

    @Bean
    public NewTopic tablATopic() {
        return new NewTopic("tableA", 1, (short) 1);
    }

}
