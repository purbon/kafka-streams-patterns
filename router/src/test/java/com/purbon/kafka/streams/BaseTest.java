package com.purbon.kafka.streams;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.util.Collections;
import java.util.Map;

public class BaseTest {

    protected KafkaProducer<GenericRecord, GenericRecord> buildAvroProducer(String servers, String schemaRegistryUrl) {
        KafkaTestUtils.producerProps(servers);
        Map<String, Object> producerProps = KafkaTestUtils.producerProps(servers);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, io.confluent.kafka.serializers.KafkaAvroSerializer.class);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, io.confluent.kafka.serializers.KafkaAvroSerializer.class);
        producerProps.put("schema.registry.url", schemaRegistryUrl);

        return new KafkaProducer<>(producerProps);
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

}
