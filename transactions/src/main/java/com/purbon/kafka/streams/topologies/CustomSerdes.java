package com.purbon.kafka.streams.topologies;

import com.purbon.kafka.streams.model.Card;
import com.purbon.kafka.streams.model.Store;
import com.purbon.kafka.streams.model.Transaction;
import com.purbon.kafka.streams.model.TransactionE;
import io.confluent.kafka.streams.serdes.avro.GenericAvroSerde;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.common.serialization.Serde;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public interface CustomSerdes {


    default Serde<GenericRecord> serde(Map<String, ?> serdeConfig, boolean isKey) {
        Serde<GenericRecord> avroSerde = new GenericAvroSerde();
        avroSerde.configure(serdeConfig, isKey);
        return avroSerde;
    }

    default Serde<Transaction> transactionSerde(Map<String, ?> serdeConfig, boolean isKey) {
        final Serde<Transaction> serde = new SpecificAvroSerde<>();
        serde.configure(serdeConfig, isKey);
        return serde;
    }

    default Serde<TransactionE> transactionESerde(Map<String, ?> serdeConfig, boolean isKey) {
        final Serde<TransactionE> serde = new SpecificAvroSerde<>();
        serde.configure(serdeConfig, isKey);
        return serde;
    }

    default Serde<Card> cardSerde(Map<String, ?> serdeConfig, boolean isKey) {
        final Serde<Card> serde = new SpecificAvroSerde<>();
        serde.configure(serdeConfig, isKey);
        return serde;
    }

    default Serde<Store> storeSerde(Map<String, ?> serdeConfig, boolean isKey) {
        final Serde<Store> serde = new SpecificAvroSerde<>();
        serde.configure(serdeConfig, isKey);
        return serde;
    }
}
