package com.purbon.kafka.streams.test;

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

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class AvroManipulationExample {

    public static class Shop {
        private String name;
        private String owner;
        private Long creationTime;
        private Long updateTime;

        public Shop(String name, String owner, Long creationTime, Long updateTime) {
            this.name = name;
            this.owner = owner;
            this.creationTime = creationTime;
            this.updateTime = updateTime;
        }

        public Shop() {
            this("", "", 0L, 0L);
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getOwner() {
            return owner;
        }

        public void setOwner(String owner) {
            this.owner = owner;
        }

        public Long getCreationTime() {
            return creationTime;
        }

        public void setCreationTime(Long creationTime) {
            this.creationTime = creationTime;
        }

        public Long getUpdateTime() {
            return updateTime;
        }

        public void setUpdateTime(Long updateTime) {
            this.updateTime = updateTime;
        }

        @Override
        public String toString() {
            return "Shop{" +
                    "name='" + name + '\'' +
                    ", owner='" + owner + '\'' +
                    ", creationTime=" + creationTime +
                    ", updateTime=" + updateTime +
                    '}';
        }
    }

    public static void main(String [] args) throws Exception {

        Schema schema = ReflectData.get().getSchema(Shop.class);

        GenericRecord record = new GenericRecordBuilder(schema)
                .set("name", "Me")
                .set("owner", "Ma")
                .set("creationTime", System.currentTimeMillis())
                .set("updateTime", System.currentTimeMillis())
                .build();

        GenericDatumWriter<GenericRecord> gdw = new GenericDatumWriter<>(schema);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Encoder encoder = EncoderFactory.get().binaryEncoder(baos, null);
        gdw.write(record, encoder);
        encoder.flush();

        BufferedOutputStream bos = new BufferedOutputStream(System.out);
        Encoder jsonEncoder = EncoderFactory.get().jsonEncoder(schema, bos);
        gdw.write(record, jsonEncoder);

        System.out.println(schema.toString());
        System.out.println("****");
        jsonEncoder.flush();

        bos.flush();
        System.out.println();
        System.out.println("****");

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        Decoder decoder = DecoderFactory.get().binaryDecoder(bais, null);

        ReflectDatumReader<Shop> rdr = new ReflectDatumReader<>(Shop.class);
        Shop shop = rdr.read(null, decoder);

        System.out.println(shop);

    }
}
