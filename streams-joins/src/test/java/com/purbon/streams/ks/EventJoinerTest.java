package com.purbon.streams.ks;

import com.purbon.streams.ks.model.envelopes.MessageImpl;
import com.purbon.streams.ks.topologies.EventJoiner;
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

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Properties;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class EventJoinerTest {

    private String thisTopicName = "this.topic";
    private String otherTopicName = "other.topic";
    private String notMatchedTopicName = "notMatched.topic";
    private String targetTopicName = "target.topic";

    private TopologyTestDriver driver;
    private TestInputTopic<String, String> thisTopic;
    private TestInputTopic<String, String> otherTopic;
    private TestOutputTopic<String, String> targetTopic;
    private TestOutputTopic<String, String> notMatchedTopic;


    @BeforeEach
    public void setup() {
        StreamsBuilder builder = new StreamsBuilder();

        EventJoiner joiner = new EventJoiner(
                thisTopicName,
                otherTopicName,
                targetTopicName,
                notMatchedTopicName
        );

        KStream<String, MessageImpl<String>> stream = joiner.build(builder);
        Topology topology = builder.build();

        final Properties props = new Properties();
        props.setProperty(StreamsConfig.APPLICATION_ID_CONFIG, "EventJoinerServiceTest"+System.currentTimeMillis());
        props.setProperty(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234");
        props.setProperty(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.setProperty(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.setProperty(StreamsConfig.STATE_DIR_CONFIG, "/tmp/s"+System.currentTimeMillis());
        driver = new TopologyTestDriver(topology, props);

        thisTopic = driver.createInputTopic(thisTopicName,
                Serdes.String().serializer(),
                Serdes.String().serializer());

        thisTopic.pipeInput("foo-key", "Luke Skywalker", 0);
        thisTopic.pipeInput("bar-key", "Anakin Skywalker", 1);

        otherTopic = driver.createInputTopic(otherTopicName,
                Serdes.String().serializer(),
                Serdes.String().serializer());

        targetTopic = driver.createOutputTopic(targetTopicName,
                Serdes.String().deserializer(), Serdes.String().deserializer());

        notMatchedTopic = driver.createOutputTopic(notMatchedTopicName,
                Serdes.String().deserializer(), Serdes.String().deserializer());
    }

    @AfterEach
    public void teardown() {
        driver.close();
    }

    @Test
    public void shouldJoinSuccessfullyEventsWithMatchingKey() {
        otherTopic.pipeInput("foo-key", "Good morning");
        var record = targetTopic.readRecord();

        assertThat(record.getKey()).isEqualTo("foo-key");
        assertThat(record.getValue()).isEqualTo("this.value=Luke Skywalker other.value=Good morning");

        assertThat(notMatchedTopic.isEmpty());
    }

    @Test
    public void shouldNotMatchLateArrivalMessages() {
        otherTopic.pipeInput("foo-key", "The dark side", 6000);
        assertThat(targetTopic.isEmpty());

        assertThat(targetTopic.getQueueSize()).isEqualTo(0);
        assertThat(notMatchedTopic.getQueueSize()).isEqualTo(2);

        var record = notMatchedTopic.readRecord();
        assertThat(record.getKey()).isEqualTo("foo-key");
        assertThat(record.getValue()).isEqualTo("this.value=Luke Skywalker - not found");
        assertThat(record.timestamp()).isEqualTo(0);

        record = notMatchedTopic.readRecord();
        assertThat(record.getKey()).isEqualTo("bar-key");
        assertThat(record.getValue()).isEqualTo("this.value=Anakin Skywalker - not found");
        assertThat(record.timestamp()).isEqualTo(1);


    }

}
