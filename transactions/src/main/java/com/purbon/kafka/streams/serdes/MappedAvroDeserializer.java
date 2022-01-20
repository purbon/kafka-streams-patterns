package com.purbon.kafka.streams.serdes;


import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.reflect.ReflectData;
import org.apache.avro.reflect.ReflectDatumReader;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.DatatypeConverter;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MappedAvroDeserializer<T> implements Deserializer<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MappedAvroDeserializer.class);
    private final Schema schema;
    private final KafkaAvroDeserializer inner;

    public MappedAvroDeserializer(Class<T> classType) {
        this.schema = ReflectData.get().getSchema(classType);
        this.inner = new KafkaAvroDeserializer();
    }

    @Override
    public void configure(Map<String, ?> config, boolean isKey) {
        Map<String, Object> fullConfig = new HashMap<>(config);
        fullConfig.put(KafkaAvroDeserializerConfig.SCHEMA_REFLECTION_CONFIG, true);
        inner.configure(fullConfig, isKey);
    }

    @Override
    public T deserialize(String topic, byte[] data) {
        return (T) inner.deserialize(topic, data, schema);
    }

    @Override
    public void close() {
        // No-op
    }
}
