package com.purbon.kafka.streams;

import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.awaitility.Awaitility;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = TestAppContainerConfig.class)
@ContextConfiguration(
        initializers = TestAppContainerConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class AvroRouterServiceTest extends BaseTest {

    private String sourceTopicOltp1 = "oltp.tableA.avro";
    private String sourceTopicOltp2 = "oltp.tableB.json";
    private String sourceTopicOltp3 = "oltp.tableB.avro";

    private String targetTopicA = "tableA";
    private String targetTopicB = "tableB";

    @Autowired
    private KafkaProperties properties;

    @Ignore
    public void testRoutingFlow() throws Exception {
        String servers = String.join(",", properties.getBootstrapServers());
        String schemaRegistryUrl = properties.getProperties().get("schema.registry.url");
        System.out.printf("Kafka Container: %s%n \n", servers);
        System.out.println("Schema Registry url =" + schemaRegistryUrl);
        int numOfRecords = 5;
        var producer = buildAvroProducer(servers, schemaRegistryUrl);

        for(int i=0; i < numOfRecords; i++) {
            var avroKey = builGenericAvroPayload("key"+i);
            var avroRecord = builGenericAvroPayload("value"+i);

            var record = new ProducerRecord<>(sourceTopicOltp1, avroKey, avroRecord);
            producer.send(record);
        }
        producer.flush();

        var consumer = buildAvroConsumer(servers, schemaRegistryUrl, targetTopicA);

        List<GenericRecord> values = new CopyOnWriteArrayList<>();
        ExecutorService service = Executors.newSingleThreadExecutor();
        Future<?> consumingTask = service.submit(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                ConsumerRecords<GenericRecord, GenericRecord> records = KafkaTestUtils.getRecords(consumer, 100);
                for (ConsumerRecord<GenericRecord, GenericRecord> rec : records) {
                    values.add(rec.value());
                }
            }
        });

        try {
            Awaitility.await().atMost(5, SECONDS)
                    .until(() -> values.size() == numOfRecords);
            assertThat(values).hasSize(numOfRecords);
            for(int i=0; i < numOfRecords; i++) {
                assertThat(values.get(i).get("f1").toString()).isEqualTo("value"+i);
            }
        } finally {
            consumingTask.cancel(true);
            service.awaitTermination(100, MILLISECONDS);
        }
    }
}