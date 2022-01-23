package com.purbon.kafka.streams;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.util.Collections;
import java.util.Map;

public class BaseTest {


    protected KafkaProducer<String, GenericRecord> buildStringAvroProducer(String servers, String schemaRegistryUrl) {
        KafkaTestUtils.producerProps(servers);
        Map<String, Object> producerProps = KafkaTestUtils.producerProps(servers);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, Serdes.String().serializer().getClass());
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, io.confluent.kafka.serializers.KafkaAvroSerializer.class);
        producerProps.put("schema.registry.url", schemaRegistryUrl);

        return new KafkaProducer<>(producerProps);
    }

    protected KafkaProducer<GenericRecord, GenericRecord> buildAvroProducer(String servers, String schemaRegistryUrl) {
        KafkaTestUtils.producerProps(servers);
        Map<String, Object> producerProps = KafkaTestUtils.producerProps(servers);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, io.confluent.kafka.serializers.KafkaAvroSerializer.class);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, io.confluent.kafka.serializers.KafkaAvroSerializer.class);
        producerProps.put("schema.registry.url", schemaRegistryUrl);

        return new KafkaProducer<>(producerProps);
    }

    protected Consumer<String, GenericRecord> buildStringAvroConsumer(String servers, String schemaRegistryUrl, String outputTopicName) {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps(servers, "group", "true");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, Serdes.String().deserializer().getClass());
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, io.confluent.kafka.serializers.KafkaAvroDeserializer.class);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put("schema.registry.url", schemaRegistryUrl);

        Consumer<String, GenericRecord> consumer = new DefaultKafkaConsumerFactory<String, GenericRecord>(consumerProps)
                .createConsumer("clientId");
        consumer.subscribe(Collections.singleton(outputTopicName));
        return consumer;
    }

    protected Consumer<GenericRecord, GenericRecord> buildAvroConsumer(String servers, String schemaRegistryUrl, String outputTopicName) {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps(servers, "group", "true");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, io.confluent.kafka.serializers.KafkaAvroDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, io.confluent.kafka.serializers.KafkaAvroDeserializer.class);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put("schema.registry.url", schemaRegistryUrl);

        Consumer<GenericRecord, GenericRecord> consumer = new DefaultKafkaConsumerFactory<GenericRecord, GenericRecord>(consumerProps)
                .createConsumer("clientId");
        consumer.subscribe(Collections.singleton(outputTopicName));
        return consumer;
    }

    protected GenericRecord builGenericAvroPayload(String value) {
        String userSchema = "{\"type\":\"record\"," +
                "\"name\":\"myrecord\"," +
                "\"fields\":[{\"name\":\"f1\",\"type\":\"string\"}]}";
        Schema.Parser parser = new Schema.Parser();
        Schema schema = parser.parse(userSchema);
        GenericRecord avroRecord = new GenericData.Record(schema);
        avroRecord.put("f1", value);
        return avroRecord;
    }

    protected GenericRecord buildConfigTableAvroKey(String databasename, String schemaName, String tableName) {
        String userSchema = "{\"type\":\"record\"," +
                "\"name\":\"myrecord\"," +
                "\"fields\":[{\"name\": \"databasename\", \"type\": \"string\"},{\"name\": \"schemaName\", \"type\": \"string\"},{\"name\": \"tableName\", \"type\": \"string\"}]}";
        Schema.Parser parser = new Schema.Parser();
        Schema schema = parser.parse(userSchema);
        GenericRecord avroRecord = new GenericData.Record(schema);
        avroRecord.put("databasename", databasename);
        avroRecord.put("schemaName", schemaName);
        avroRecord.put("tableName", tableName);
        return avroRecord;
    }

    protected GenericRecord buildConfigTableAvroPayload(String tableName,
                                                        String seqName,
                                                        String identifierColumnName,
                                                        String min,
                                                        String max) {
        String userSchema = "{\"type\":\"record\"," +
                "\"name\":\"myrecord\"," +
                "\"fields\":[{\"name\": \"tableName\", \"type\": \"string\"},{\"name\": \"seqName\", \"type\": \"string\"},{\"name\": \"IdentifierColumnName\", \"type\": \"string\"},{\"name\": \"min\", \"type\": \"string\"},\n" +
                "{\"name\": \"max\", \"type\": \"string\"}]}";
        Schema.Parser parser = new Schema.Parser();
        Schema schema = parser.parse(userSchema);
        GenericRecord avroRecord = new GenericData.Record(schema);
        avroRecord.put("tableName", tableName);
        avroRecord.put("seqName", seqName);
        avroRecord.put("IdentifierColumnName", identifierColumnName);
        avroRecord.put("min", min);
        avroRecord.put("max", max);
        return avroRecord;
    }

    protected GenericRecord buildTxAvroPayload(String transactionMainID, String creditCardID) {
        return buildTxAvroPayload(transactionMainID, creditCardID, "r");
    }

    protected GenericRecord buildTxAvroPayload(String transactionMainID, String creditCardID, String operation) {
        String userSchema = "{\"type\":\"record\"," +
                "\"name\":\"myrecord\"," +
                "\"fields\":[{\"name\":\"TransactionMainID\",\"type\":\"string\"},{\"name\":\"CreditCardID\",\"type\":\"string\"},{\"name\":\"__op\",\"type\":[\"null\",\"string\"],\"default\":null}]}";
        Schema.Parser parser = new Schema.Parser();
        Schema schema = parser.parse(userSchema);
        GenericRecord avroRecord = new GenericData.Record(schema);
        avroRecord.put("TransactionMainID", transactionMainID);
        avroRecord.put("CreditCardID", creditCardID);
        avroRecord.put("__op", operation);
        return avroRecord;
    }

    protected GenericRecord buildTxAvroKey(String transactionMainID, String transactionDate) {
        String userSchema = "{\"type\":\"record\"," +
                "\"name\":\"myrecord\"," +
                "\"fields\":[{\"name\":\"TransactionMainID\",\"type\":\"string\"},{\"name\":\"TransactionDate\",\"type\":\"string\"}]}";
        Schema.Parser parser = new Schema.Parser();
        Schema schema = parser.parse(userSchema);
        GenericRecord avroRecord = new GenericData.Record(schema);
        avroRecord.put("TransactionMainID", transactionMainID);
        avroRecord.put("TransactionDate", transactionDate);
        return avroRecord;
    }


}
