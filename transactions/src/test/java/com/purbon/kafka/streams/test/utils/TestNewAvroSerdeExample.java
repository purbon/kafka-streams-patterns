package com.purbon.kafka.streams.test.utils;

import com.purbon.kafka.streams.serdes.MappedAvroDeserializer;
import io.confluent.kafka.streams.serdes.avro.ReflectionAvroSerializer;
import org.apache.avro.Schema;
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
import java.util.Collections;
import java.util.Map;

public class TestNewAvroSerdeExample {

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

        Map<String, ?> config = Collections.singletonMap("schema.registry.url", "http://localhost:8081");

        ReflectionAvroSerializer<Shop> ser = new ReflectionAvroSerializer<>();
        ser.configure(config, false);

        MappedAvroDeserializer<com.purbon.kafka.streams.model.Shop> des = new MappedAvroDeserializer<>(com.purbon.kafka.streams.model.Shop.class);
        des.configure(config, false);

        var shop = new Shop();
        shop.setName("name");
        shop.setOwner("owner");
        shop.setCreationTime(System.currentTimeMillis());
        shop.setUpdateTime(System.currentTimeMillis());

        var bytes = ser.serialize("topic", shop);
        var newShop = des.deserialize("topic", bytes);

        System.out.println(newShop);



    }
}
