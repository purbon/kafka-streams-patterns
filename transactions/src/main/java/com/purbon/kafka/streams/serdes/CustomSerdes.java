package com.purbon.kafka.streams.serdes;

import com.purbon.kafka.streams.model.Card;
import com.purbon.kafka.streams.model.Store;
import com.purbon.kafka.streams.model.Transaction;
import com.purbon.kafka.streams.model.TransactionE;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.common.serialization.Serde;

import java.util.Map;

public interface CustomSerdes {


    Serde<GenericRecord> serde(Map<String, ?> serdeConfig, boolean isKey);
    Serde<Transaction> transactionSerde(Map<String, ?> serdeConfig, boolean isKey);
    Serde<TransactionE> transactionESerde(Map<String, ?> serdeConfig, boolean isKey);
    Serde<Card> cardSerde(Map<String, ?> serdeConfig, boolean isKey);
    Serde<Store> storeSerde(Map<String, ?> serdeConfig, boolean isKey);
}
