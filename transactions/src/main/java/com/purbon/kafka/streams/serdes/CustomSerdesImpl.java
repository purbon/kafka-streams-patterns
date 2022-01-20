package com.purbon.kafka.streams.serdes;

import com.purbon.kafka.streams.model.Card;
import com.purbon.kafka.streams.model.Store;
import com.purbon.kafka.streams.model.Transaction;
import com.purbon.kafka.streams.model.TransactionE;
import io.confluent.kafka.streams.serdes.avro.GenericAvroSerde;
import io.confluent.kafka.streams.serdes.avro.ReflectionAvroSerde;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.common.serialization.Serde;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CustomSerdesImpl implements CustomSerdes {

    public Serde<GenericRecord> serde(Map<String, ?> serdeConfig, boolean isKey) {
        Serde<GenericRecord> avroSerde = new GenericAvroSerde();
        avroSerde.configure(serdeConfig, isKey);
        return avroSerde;
    }

    public Serde<Transaction> transactionSerde(Map<String, ?> serdeConfig, boolean isKey) {
        final Serde<Transaction> serde = new ReflectionAvroSerde<>(Transaction.class);
        serde.configure(serdeConfig, isKey);
        return serde;
    }

    public Serde<TransactionE> transactionESerde(Map<String, ?> serdeConfig, boolean isKey) {
        final Serde<TransactionE> serde = new ReflectionAvroSerde<>(TransactionE.class);
        serde.configure(serdeConfig, isKey);
        return serde;
    }

    public Serde<Card> cardSerde(Map<String, ?> serdeConfig, boolean isKey) {
        final Serde<Card> serde = new ReflectionAvroSerde<>(Card.class);
        serde.configure(serdeConfig, isKey);
        return serde;
    }

    public Serde<Store> storeSerde(Map<String, ?> serdeConfig, boolean isKey) {
        final Serde<Store> serde = new ReflectionAvroSerde<>(Store.class);
        serde.configure(serdeConfig, isKey);
        return serde;
    }
}
