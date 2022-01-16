package com.purbon.kafka.streams.utils;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.HashMap;
import java.util.Map;

public class DummyProducer {

    public static void main(String[] args) {
        Map<String, Object> config = new HashMap<>();
        config.put("sasl.mechanism", "GSSAPI");
        config.put("security.protocol","SASL_PLAINTEXT");
        config.put("sasl.kerberos.service.name", "kafka");
        String jaas = "com.sun.security.auth.module.Krb5LoginModule required " +
                "    useKeyTab=true " +
                "    storeKey=true " +
                "    keyTab=\"/var/lib/secret/julieops.keytab\" " +
                "    principal=\"julieops@TEST.CONFLUENT.IO\";";
        config.put("sasl.jaas.config", jaas);
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka.kerberos-demo.local:9093");

        KafkaProducer<String, String> producer = new KafkaProducer<String, String>(config);

        for(int i=0; i < 10; i++) {
            ProducerRecord<String, String> record = new ProducerRecord<>("my.topic", "key"+i, "value"+i);
            producer.send(record);
        }

        producer.flush();
        producer.close();
    }
}
