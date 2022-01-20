package com.purbon.kafka.streams.serdes;


import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.reflect.ReflectDatumReader;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.DatatypeConverter;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Map;

public class GenericAvroDeserializer<T> implements Deserializer<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GenericAvroDeserializer.class);

    @Override
    public void close() {
        // No-op
    }

    @Override
    public void configure(Map<String, ?> arg0, boolean arg1) {
        // No-op
    }

    @SuppressWarnings("unchecked")
    @Override
    public T deserialize(String topic, byte[] data) {
        try {
            T result = null;

            if (data != null) {
                LOGGER.debug("data='{}'", DatatypeConverter.printHexBinary(data));

                var schemaBytesLengthArray = Arrays.copyOfRange(data, 0, 4);
                var dataLengthArray = Arrays.copyOfRange(data, 4, 8);

                int schemaBytesLength = ByteBuffer.wrap(schemaBytesLengthArray).getInt();
                int dataBytesLength = ByteBuffer.wrap(dataLengthArray).getInt();

                var start = 8;
                var end = start + schemaBytesLength;
                var schemaBytesArray = Arrays.copyOfRange(data, start, end);

                start = end + 1;
                end = start + dataBytesLength;
                var dataBytesArray = Arrays.copyOfRange(data, start, end);


                Schema.Parser parser = new Schema.Parser();
                Schema schema = parser.parse(new String(schemaBytesArray));

               // DatumReader<GenericRecord> datumReader = new SpecificDatumReader<>(schema);
                ReflectDatumReader<GenericRecord> datumReader = new ReflectDatumReader<>(schema);
                Decoder decoder = DecoderFactory.get().binaryDecoder(dataBytesArray, null);

                result = (T) datumReader.read(null, decoder);
                LOGGER.debug("deserialized data='{}'", result);
            }
            return result;
        } catch (Exception ex) {
            throw new SerializationException(
                    "Can't deserialize data '" + Arrays.toString(data) + "' from topic '" + topic + "'", ex);
        }
    }
}
