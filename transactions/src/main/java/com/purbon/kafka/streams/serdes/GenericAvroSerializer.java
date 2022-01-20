package com.purbon.kafka.streams.serdes;


import org.apache.avro.Schema;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.reflect.ReflectDatumWriter;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;

public class GenericAvroSerializer<T> implements Serializer<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GenericAvroSerializer.class);
    private final Schema schema;

    public GenericAvroSerializer(Schema schema) {
        this.schema = schema;
    }
    @Override
    public void close() {
        // No-op
    }

    @Override
    public void configure(Map<String, ?> arg0, boolean arg1) {
        // No-op
    }

    @Override
    public byte[] serialize(String topic, T data) {
        try {
            byte[] result = null;

            if (data != null) {
                LOGGER.debug("data='{}'", data);

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                BinaryEncoder binaryEncoder =
                        EncoderFactory.get().binaryEncoder(byteArrayOutputStream, null);

                ReflectDatumWriter<T> datumWriter = new ReflectDatumWriter<>(schema);
                datumWriter.write(data, binaryEncoder);

                binaryEncoder.flush();
                byteArrayOutputStream.close();

                var dataResult = byteArrayOutputStream.toByteArray();
                var schemaBytes = schema.toString().getBytes();

                ByteArrayOutputStream bos = new ByteArrayOutputStream();

                var schemaLengthBytes = ByteBuffer.allocate(4).putInt(schemaBytes.length).array();
                var dataLengthBytes = ByteBuffer.allocate(4).putInt(dataResult.length).array();

                System.out.println(schemaBytes.length);
                System.out.println(ByteBuffer.wrap(schemaLengthBytes).getInt());

                bos.writeBytes(schemaLengthBytes);
                bos.writeBytes(dataLengthBytes);
                bos.writeBytes(schemaBytes);
                bos.writeBytes(dataResult);

                result = bos.toByteArray();
                bos.close();

                LOGGER.debug("serialized data='{}'", DatatypeConverter.printHexBinary(result));
            }
            return result;
        } catch (IOException ex) {
            throw new SerializationException(
                    "Can't serialize data='" + data + "' for topic='" + topic + "'", ex);
        }
    }
}
