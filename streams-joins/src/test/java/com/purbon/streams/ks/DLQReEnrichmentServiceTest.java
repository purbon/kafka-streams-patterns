package com.purbon.streams.ks;

import com.purbon.streams.ks.model.envelopes.MessageImpl;
import com.purbon.streams.ks.topologies.DLQBasedEnrichmentTopologyBuilder;
import com.purbon.streams.ks.topologies.EnrichmentTopologyBuilder;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import org.apache.kafka.streams.test.TestRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.config.KafkaStreamsConfiguration;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;

import java.nio.charset.StandardCharsets;
import java.util.Properties;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.in;

public class DLQReEnrichmentServiceTest {

    private String tableTopicName = "table.topic";
    private String dlqTopicName = "parking.topic";
    private String sourceTopicName = "dlq.topic";
    private String targetTopicName = "target.topic";

    private TopologyTestDriver driver;
    private TestInputTopic<String, String> tableTopic;
    private TestInputTopic<String, String> inputTopic;
    private TestOutputTopic<String, String> outputTopic;
    private TestOutputTopic<String, String> dlqTopic;


    @BeforeEach
    public void setup() {
        StreamsBuilder builder = new StreamsBuilder();

        DLQBasedEnrichmentTopologyBuilder topologyBuilder = new DLQBasedEnrichmentTopologyBuilder(
                tableTopicName,
                dlqTopicName,
                sourceTopicName,
                targetTopicName);

        StoreBuilder dlqStoreBuilder =
                Stores.keyValueStoreBuilder(
                        Stores.persistentKeyValueStore("dlq.store"),
                        Serdes.String(),
                        Serdes.String()
                );
        builder.addStateStore(dlqStoreBuilder);

        KStream<String, MessageImpl<String>> stream = topologyBuilder.build(builder);
        Topology topology = builder.build();

        final Properties props = new Properties();
        props.setProperty(StreamsConfig.APPLICATION_ID_CONFIG, "DLQReEnrichmentServiceTest");
        props.setProperty(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234");
        props.setProperty(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.setProperty(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());

        driver = new TopologyTestDriver(topology, props);

        tableTopic = driver.createInputTopic(tableTopicName,
                Serdes.String().serializer(),
                Serdes.String().serializer());

        tableTopic.pipeInput("foo-key", "Luke Skywalker");
        tableTopic.pipeInput("bar-key", "Anakin Skywalker");

        inputTopic = driver.createInputTopic(sourceTopicName,
                Serdes.String().serializer(),
                Serdes.String().serializer());

        outputTopic = driver.createOutputTopic(targetTopicName,
                Serdes.String().deserializer(), Serdes.String().deserializer());

        dlqTopic = driver.createOutputTopic(dlqTopicName,
                Serdes.String().deserializer(), Serdes.String().deserializer());
    }

    @AfterEach
    public void teardown() {
        driver.close();
    }

    @Test
    public void shouldEnrichMessagesWithMatchingKeys() {

        TestRecord<String, String> record = new TestRecord<>("foo-key", "Good morning");
        record.headers().add("retries", "1".getBytes(StandardCharsets.UTF_8));
        inputTopic.pipeInput(record);

        assertThat(dlqTopic.isEmpty());

        var outputRecord = outputTopic.readRecord();

        assertThat(outputRecord.getKey()).isEqualTo("foo-key");
        assertThat(outputRecord.getValue()).isEqualTo("Good morning - Luke Skywalker");

        var header = outputRecord.getHeaders().lastHeader("retries");
        assertThat(header).isNotNull();
        assertThat(header.value()).isNotNull();
        assertThat(new String(header.value(), StandardCharsets.UTF_8)).isEqualTo("1");
    }

    @Test
    public void shouldSendToDLQNotEnrichedMessages() {

        TestRecord<String, String> record = new TestRecord<>("zoo-key", "Hallonchen");
        record.headers().add("retries", "1".getBytes(StandardCharsets.UTF_8));

        inputTopic.pipeInput(record);

        var outputRecord = dlqTopic.readRecord();

        assertThat(outputRecord.getKey()).isEqualTo("zoo-key");
        assertThat(outputRecord.getValue()).isEqualTo("Hallonchen");

        var header = outputRecord.getHeaders().lastHeader("retries");
        assertThat(header).isNotNull();
        assertThat(header.value()).isNotNull();
        assertThat(new String(header.value(), StandardCharsets.UTF_8)).isEqualTo("6");

        assertThat(outputTopic.isEmpty());
    }
}
