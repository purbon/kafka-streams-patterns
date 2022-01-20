package com.purbon.kafka.streams;

import com.purbon.kafka.streams.model.Card;
import com.purbon.kafka.streams.model.Store;
import com.purbon.kafka.streams.model.Transaction;
import com.purbon.kafka.streams.model.TransactionE;
import com.purbon.kafka.streams.serdes.AvroDeserializer;
import com.purbon.kafka.streams.serdes.AvroSerializer;
import com.purbon.kafka.streams.serdes.CustomSerdes;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;

import java.util.Map;

public class TestCustomSerdes implements CustomSerdes {
    @Override
    public Serde<GenericRecord> serde(Map<String, ?> serdeConfig, boolean isKey) {
        return null;
    }

    @Override
    public Serde<Transaction> transactionSerde(Map<String, ?> serdeConfig, boolean isKey) {
        var serdes = Serdes.serdeFrom(new AvroSerializer<>(), new AvroDeserializer<>(Transaction.class));
        serdes.configure(serdeConfig, isKey);
        return serdes;
    }

    @Override
    public Serde<TransactionE> transactionESerde(Map<String, ?> serdeConfig, boolean isKey) {
        var serdes = Serdes.serdeFrom(new AvroSerializer<>(), new AvroDeserializer<>(TransactionE.class));
        serdes.configure(serdeConfig, isKey);
        return serdes;
    }

    @Override
    public Serde<Card> cardSerde(Map<String, ?> serdeConfig, boolean isKey) {
        var serdes = Serdes.serdeFrom(new AvroSerializer<>(), new AvroDeserializer<>(Card.class));
        serdes.configure(serdeConfig, isKey);
        return serdes;
    }

    @Override
    public Serde<Store> storeSerde(Map<String, ?> serdeConfig, boolean isKey) {
        var serdes = Serdes.serdeFrom(new AvroSerializer<>(), new AvroDeserializer<>(Store.class));
        serdes.configure(serdeConfig, isKey);
        return serdes;
    }
}
