package com.purbon.streams.ks;

import com.purbon.streams.ks.model.envelopes.MessageImpl;
import com.purbon.streams.ks.topologies.Ks2KsEnrichmentTopologyBuilder;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.kstream.KStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.support.serializer.JsonSerde;

import java.nio.charset.StandardCharsets;
import java.util.Properties;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class Ks2KsEnrichmentServiceTest {

    private String enrichmentTopicName = "enrichment.topic";
    private String dlqTopicName = "dlq.topic";
    private String sourceTopicName = "source.topic";
    private String targetTopicName = "target.topic";

    private TopologyTestDriver driver;
    private TestInputTopic<String, String> enrichmentTopic;
    private TestInputTopic<String, String> inputTopic;
    private TestOutputTopic<String, String> outputTopic;
    private TestOutputTopic<String, String> dlqTopic;


    @BeforeEach
    public void setup() {
        StreamsBuilder builder = new StreamsBuilder();

        Ks2KsEnrichmentTopologyBuilder topologyBuilder = new Ks2KsEnrichmentTopologyBuilder(enrichmentTopicName,
                dlqTopicName,
                sourceTopicName,
                targetTopicName);

        KStream<String, MessageImpl<String>> stream = topologyBuilder.build(builder);
        Topology topology = builder.build();

        JsonSerde<MessageImpl<String>> msgSerdes = new JsonSerde<>(MessageImpl.class);
        Serde<MessageImpl<String>> envSerdes = Serdes.serdeFrom(msgSerdes.serializer(), msgSerdes.deserializer());


        final Properties props = new Properties();
        props.setProperty(StreamsConfig.APPLICATION_ID_CONFIG, "Ks2KsEnrichmentServiceTest");
        props.setProperty(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234");
        props.setProperty(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.setProperty(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());

        driver = new TopologyTestDriver(topology, props);

        enrichmentTopic = driver.createInputTopic(enrichmentTopicName,
                Serdes.String().serializer(),
                Serdes.String().serializer());

        enrichmentTopic.pipeInput("foo-key", "Luke Skywalker");
        enrichmentTopic.pipeInput("bar-key", "Anakin Skywalker", 0);

        enrichmentTopic.pipeInput("bar-key", "Anakin", 10);

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
        inputTopic.pipeInput("foo-key", "Good morning");
        var record = outputTopic.readRecord();

        assertThat(record.getKey()).isEqualTo("foo-key");
        assertThat(record.getValue()).isEqualTo("Good morning - Luke Skywalker");

        var header = record.getHeaders().lastHeader("retries");
        assertThat(header).isNotNull();
        assertThat(header.value()).isNotNull();
        assertThat(new String(header.value(), StandardCharsets.UTF_8)).isEqualTo("0");


        assertThat(dlqTopic.isEmpty());
    }

    @Test
    public void shouldSendToDLQNotEnrichedMessages() {
        inputTopic.pipeInput("zoo-key", "Hallonchen");

        var record = dlqTopic.readRecord();

        assertThat(record.getKey()).isEqualTo("zoo-key");
        assertThat(record.getValue()).isEqualTo("Hallonchen");

        var header = record.getHeaders().lastHeader("retries");
        assertThat(header).isNotNull();
        assertThat(header.value()).isNotNull();
        assertThat(new String(header.value(), StandardCharsets.UTF_8)).isEqualTo("1");

        assertThat(outputTopic.isEmpty());
    }
}
