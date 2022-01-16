package com.purbon.kafka.streams;

import com.purbon.kafka.streams.model.Card;
import com.purbon.kafka.streams.model.Store;
import com.purbon.kafka.streams.model.Transaction;
import com.purbon.kafka.streams.model.TransactionE;
import com.purbon.kafka.streams.serdes.TestCustomSerdes;
import com.purbon.kafka.streams.topologies.CustomSerdes;
import com.purbon.kafka.streams.topologies.DelayedTxInfraCustomizer;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.TopologyTestDriver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static com.purbon.kafka.streams.topologies.DelayedTxInfraCustomizer.DELAYED_TRANSACTION_TOPIC;
import static com.purbon.kafka.streams.topologies.TransactionTopologyBuilder.TRANSACTION_TOPIC;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DelayedTopologyTest {

    private TestOutputTopic<Long, TransactionE> outputTopic;

    private TestInputTopic<Long, Card> cardTopic;
    private TestInputTopic<Integer, Store> storeTopic;
    private TestInputTopic<Long, Transaction> txTopic;

    @BeforeEach
    public void before() {
        CustomSerdes customSerdes = new TestCustomSerdes();

        StreamsBuilder builder = new StreamsBuilder();

        Properties appConfig = new Properties();
        appConfig.put(StreamsConfig.APPLICATION_ID_CONFIG, "test");
        appConfig.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234");
        appConfig.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        appConfig.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());

        Map<String, Object> config = new HashMap<>();

        DelayedTxInfraCustomizer customizer = new DelayedTxInfraCustomizer(customSerdes, config);
        customizer.configureBuilder(builder);
        var topology = customizer.getLatestTopology();

        var txSerde = customSerdes.transactionSerde(config, false);
        var txESerde = customSerdes.transactionESerde(config, false);

        var cardSerde = customSerdes.cardSerde(config, false);
        var storeSerde = customSerdes.storeSerde(config, false);


        TopologyTestDriver testDriver = new TopologyTestDriver(topology, appConfig);

        txTopic = testDriver
                .createInputTopic(DELAYED_TRANSACTION_TOPIC, Serdes.Long().serializer(), txSerde.serializer());
        outputTopic = testDriver
                .createOutputTopic(TRANSACTION_TOPIC, Serdes.Long().deserializer(), txESerde.deserializer());
    }

    public void after() {

    }


    @Test
    public void testDelayedMessageReverseTwoUpdates() throws InterruptedException {

        Long txId = 1L;
        Long cardId = 1L;
        Integer storeId = 1;

        Transaction tx = new Transaction(txId, cardId, "", 1L, storeId);
        long baseTimestamp = System.currentTimeMillis();
        txTopic.pipeInput(txId, tx, baseTimestamp);

        txId = 1L;
        tx = new Transaction(txId, cardId, "", 1L, storeId);
        //baseTimestamp = System.currentTimeMillis();
        txTopic.pipeInput(txId, tx, baseTimestamp+14000);

        var outputSize = outputTopic.getQueueSize();
        assertThat(outputSize).isEqualTo(2);

        var record = outputTopic.readRecord();
        assertThat(record.timestamp()).isGreaterThan(baseTimestamp);

        System.out.println(baseTimestamp);
        System.out.println(record.timestamp());
        System.out.println(record.timestamp()-baseTimestamp);

        record = outputTopic.readRecord();
        assertThat(record.timestamp()).isGreaterThan(baseTimestamp);

        System.out.println(baseTimestamp);
        System.out.println(record.timestamp());
        System.out.println(record.timestamp()-baseTimestamp);

    }
}
