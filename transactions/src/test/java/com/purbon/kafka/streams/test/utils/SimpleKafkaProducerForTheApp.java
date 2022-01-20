package com.purbon.kafka.streams.test.utils;

import com.purbon.kafka.streams.model.Tuple;
import com.purbon.kafka.streams.test.utils.model.Card;
import com.purbon.kafka.streams.test.utils.model.Store;
import com.purbon.kafka.streams.test.utils.model.Transaction;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import static com.purbon.kafka.streams.topologies.TransactionTopologyBuilder.CREDITCARDS_TOPIC;
import static com.purbon.kafka.streams.topologies.TransactionTopologyBuilder.STORES_TOPIC;
import static com.purbon.kafka.streams.topologies.TransactionTopologyBuilder.TRANSACTION_TOPIC;

public class SimpleKafkaProducerForTheApp<K, V> {

    KafkaProducer<K, V> txProducer;

    public SimpleKafkaProducerForTheApp(Properties props) {
        txProducer = new KafkaProducer<>(props);
    }

    public void send(String topic, List<Tuple<K,V>> txs) {
        var records = txs.stream()
                .map(e -> new ProducerRecord<>(topic, e.getLeft(), e.getRight()))
                .collect(Collectors.toList());
        for(ProducerRecord record : records) {
            txProducer.send(record);
        }
        txProducer.flush();

    }

    public static void main(String[] args) {

        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29092");
        props.put("schema.registry.url", "http://localhost:8081");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, io.confluent.kafka.serializers.KafkaAvroSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,  io.confluent.kafka.serializers.KafkaAvroSerializer.class.getName());

        SimpleKafkaProducerForTheApp<Long, Transaction> transProd = new SimpleKafkaProducerForTheApp<>(props);
        SimpleKafkaProducerForTheApp<Long, Card> cardProd = new SimpleKafkaProducerForTheApp<>(props);
        SimpleKafkaProducerForTheApp<Integer, Store> storeProd = new SimpleKafkaProducerForTheApp<>(props);

        Long txId = 1L;
        Long cardId = 1L;
        Integer storeId = 1;

        Transaction tx = new Transaction(txId, cardId, "", 1L, storeId);
        Card card = new Card(cardId, "Owner", "1234", 12345);
        Store store = new Store(storeId, "Store");

        var txs = Arrays.asList(new Tuple<>(txId, tx));
        transProd.send(TRANSACTION_TOPIC, txs);

        var cards = Arrays.asList(new Tuple<>(cardId, card));
        cardProd.send(CREDITCARDS_TOPIC, cards);

        var stores = Arrays.asList(new Tuple<>(storeId, store));
        storeProd.send(STORES_TOPIC, stores);




    }
}
