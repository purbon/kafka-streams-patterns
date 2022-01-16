package com.purbon.kafka.streams;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.GenericRecordBuilder;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.reflect.ReflectData;
import org.apache.avro.reflect.ReflectDatumReader;

import java.awt.geom.GeneralPath;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AvroReflect {

    static class Play {
        private String title;
        private String author;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getAuthor() {
            return author;
        }

        public void setAuthor(String author) {
            this.author = author;
        }
    }

    public static void main(String[] args) throws IOException {

        Schema schema = ReflectData.get().getSchema(Play.class);
        System.out.println(schema);

        GenericRecord record = new GenericRecordBuilder(schema)
                .set("title", "Mar i cel")
                .set("author", "Angel Gimera")
                .build();

        GenericDatumWriter<GenericRecord> gdw = new GenericDatumWriter<>(schema);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Encoder encoder = EncoderFactory.get().binaryEncoder(baos, null);
        gdw.write(record, encoder);
        encoder.flush();


        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        Decoder decoder = DecoderFactory.get().binaryDecoder(bais, null);

        ReflectDatumReader<Play> reader = new ReflectDatumReader<>(Play.class);
        Play play = reader.read(null, decoder);
        System.out.println("title: "+ play.getTitle());
        System.out.println("author: "+play.getAuthor());

        /****/


        String key = "key1";
        String userSchema = "{\"type\":\"record\"," +
                "\"name\":\"myrecord\"," +
                "\"fields\":[{\"name\":\"f1\",\"type\":\"string\"}]}";

        Map<String, Object> schemaObject = new HashMap<>();
        schemaObject.put("type", "record");
        schemaObject.put("name", "myrecord");
        schemaObject.put("fields", fields);

        Schema.Parser parser = new Schema.Parser();
        Schema schema = parser.parse(userSchema);
        GenericRecord avroRecord = new GenericData.Record(schema);
        avroRecord.put("f1", "value1");

    }
}
