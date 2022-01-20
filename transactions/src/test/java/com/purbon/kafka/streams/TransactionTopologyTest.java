package com.purbon.kafka.streams;

import com.purbon.kafka.streams.model.Card;
import com.purbon.kafka.streams.model.Store;
import com.purbon.kafka.streams.model.Transaction;
import com.purbon.kafka.streams.model.TransactionE;
import com.purbon.kafka.streams.serdes.CustomSerdes;
import com.purbon.kafka.streams.topologies.TransactionTopologyBuilder;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.TopologyTestDriver;
import org.junit.Ignore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static com.purbon.kafka.streams.topologies.TransactionTopologyBuilder.CREDITCARDS_TOPIC;
import static com.purbon.kafka.streams.topologies.TransactionTopologyBuilder.FINAL_TOPIC;
import static com.purbon.kafka.streams.topologies.TransactionTopologyBuilder.STORES_TOPIC;
import static com.purbon.kafka.streams.topologies.TransactionTopologyBuilder.TRANSACTION_TOPIC;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TransactionTopologyTest {

    private TestOutputTopic<Long, TransactionE> outputTopic;

    private TestInputTopic<Long, Card> cardTopic;
    private TestInputTopic<Integer, Store> storeTopic;
    private TestInputTopic<Long, Transaction> txTopic;
    private TopologyTestDriver testDriver;

    @BeforeEach
    public void before() {
        CustomSerdes customSerdes = new TestCustomSerdes();

        StreamsBuilder builder = new StreamsBuilder();

        Properties appConfig = new Properties();
        appConfig.put(StreamsConfig.APPLICATION_ID_CONFIG, "TransactionTopologyTest");
        appConfig.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234");
        appConfig.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        appConfig.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());

        Map<String, Object> config = new HashMap<>();

        var topologyBuilder = new TransactionTopologyBuilder(customSerdes, config, 5);
        topologyBuilder.build(builder);
        var topology = builder.build();


        var txSerde = customSerdes.transactionSerde(config, false);
        var txESerde = customSerdes.transactionESerde(config, false);

        var cardSerde = customSerdes.cardSerde(config, false);
        var storeSerde = customSerdes.storeSerde(config, false);

        testDriver = new TopologyTestDriver(topology, appConfig);

        txTopic = testDriver
                .createInputTopic(TRANSACTION_TOPIC, Serdes.Long().serializer(), txSerde.serializer());

        cardTopic = testDriver
                .createInputTopic(CREDITCARDS_TOPIC, Serdes.Long().serializer(), cardSerde.serializer());
        storeTopic = testDriver
                .createInputTopic(STORES_TOPIC, Serdes.Integer().serializer(), storeSerde.serializer());

        outputTopic = testDriver
                .createOutputTopic(FINAL_TOPIC, Serdes.Long().deserializer(), txESerde.deserializer());
    }

    @AfterEach
    public void after() {
        testDriver.close();
    }


    @Test
    public void testFinalTransactionComposition() {

        Long txId = 1L;
        Long cardId = 1L;
        Integer storeId = 1;

        Transaction tx = new Transaction(txId, cardId, "", 1L, storeId);

        Card card = new Card(cardId, "Owner", "1234", 12345);
        Store store = new Store(storeId, "Store");

        txTopic.pipeInput(txId, tx);
        cardTopic.pipeInput(cardId, card);
        storeTopic.pipeInput(storeId, store);

        var outputSize = outputTopic.getQueueSize();
        assertThat(outputSize).isEqualTo(1);

        var result = outputTopic.readValue();
        assertThat(result.getCardOwner().toString()).isEqualTo("Owner");
        assertThat(result.getStoreName().toString()).isEqualTo("Store");

    }

    @Test
    public void testDelayedMessage() {

        Long txId = 1L;
        Long cardId = 1L;
        Integer storeId = 1;

        Transaction tx = new Transaction(txId, cardId, "", 1L, storeId);

        Card card = new Card(cardId, "Owner", "1234", 12345);
        Store store = new Store(storeId, "Store");

        txTopic.pipeInput(txId, tx);
        cardTopic.pipeInput(cardId, card);
        storeTopic.pipeInput(storeId, store, System.currentTimeMillis()+5001);

        var outputSize = outputTopic.getQueueSize();
        assertThat(outputSize).isEqualTo(0);
    }


    @Ignore
    public void testDelayedMessageReverseTwoUpdates() {

        Long txId = 1L;
        Long cardId = 1L;
        Integer storeId = 1;

        Transaction tx = new Transaction(txId, cardId, "", 1L, storeId);

        Card card = new Card(cardId, "Owner", "1234", 12345);
        Store store = new Store(storeId, "Store");

        long baseTimestamp = System.currentTimeMillis();

        txTopic.pipeInput(txId, tx, baseTimestamp+4001);
        cardTopic.pipeInput(cardId, card, baseTimestamp);
        storeTopic.pipeInput(storeId, store, baseTimestamp+5000);
        txTopic.pipeInput(txId, tx, baseTimestamp+4000);

        var outputSize = outputTopic.getQueueSize();
        assertThat(outputSize).isEqualTo(1);
    }
}
