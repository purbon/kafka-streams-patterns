package com.purbon.kafka.streams.test.utils;


import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

public class SchemaRegistryContainer extends GenericContainer<SchemaRegistryContainer> {

    private static final DockerImageName DEFAULT_IMAGE =
            DockerImageName.parse("confluentinc/cp-schema-registry").withTag("6.2.0");

    public static final int SR_PORT = 8081;

    public SchemaRegistryContainer(KafkaContainer kafka) {
        this(DEFAULT_IMAGE, kafka);
    }

    public SchemaRegistryContainer(
            final DockerImageName dockerImageName, KafkaContainer kafka) {
        super(dockerImageName);
        String kafkaHost = kafka.getNetworkAliases().get(1);
        withExposedPorts(SR_PORT);
        withEnv("SCHEMA_REGISTRY_HOST_NAME", "schema-registry");
        withEnv(
                "SCHEMA_REGISTRY_KAFKASTORE_BOOTSTRAP_SERVERS",
                "PLAINTEXT://" + kafkaHost + ":" + 9092);
        withNetwork(kafka.getNetwork());
    }

    public String getUrl() {
        return "http://" + getHost() + ":" + getMappedPort(8081);
    }
}
