package com.purbon.kafka.streams;

import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.assertj.core.api.Condition;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = TestAppContainerConfig.class)
@ContextConfiguration(
        initializers = TestAppContainerConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class FilterServiceTest extends BaseTest {

    private String sourceTopicOltp1 = "oltp.tableA.avro";
    private String configTopic = "configs";

    private String targetTopicA = "tableA";

    @Autowired
    private KafkaProperties properties;

    private void loadConfigurationTable(String servers, String schemaRegistryUrl) {
        var producer = buildAvroProducer(servers, schemaRegistryUrl);
        var avroRecord = buildConfigTableAvroPayload("TransactionDescriptor",
                "SEQ_TransactionMain",
                "TransactionMainID",
                "4110000000000000001",
                "4119999999999990000");

        var avroKey = buildConfigTableAvroKey("online", "dbo", "TransactionDescriptor");

        producer.send(new ProducerRecord<>(configTopic, avroKey, avroRecord));

        avroRecord = buildConfigTableAvroPayload("TransactionMain",
                "SEQ_TransactionMain",
                "TransactionMainID",
                "4110000000000000001",
                "4119999999999990000");

        avroKey = buildConfigTableAvroKey("online", "dbo", "TransactionMain");

        producer.send(new ProducerRecord<>(configTopic, avroKey, avroRecord));

        producer.flush();
        producer.close();
    }


    private void produceTxEvents(int count, long txMainId, String sourceTable, String servers, String schemaRegistryUrl) {

        var producer = buildAvroProducer(servers, schemaRegistryUrl);

        for(int i=0; i < count; i++) {
            String txIdAsString = String.valueOf(txMainId);
            var avroKey = buildTxAvroKey(txIdAsString, "1594733209187");
            var avroVal = buildTxAvroPayload(txIdAsString, "1095171956");

            var record = new ProducerRecord<>(sourceTopicOltp1, avroKey, avroVal);
            record.headers().add("__source_table", sourceTable.getBytes(StandardCharsets.UTF_8));
            record.headers().add("__source_schema", "dbo".getBytes(StandardCharsets.UTF_8));
            record.headers().add("__source_db", "online".getBytes(StandardCharsets.UTF_8));

            producer.send(record);
            txMainId += 1;
        }

        producer.flush();
        producer.close();
    }

    private void produceTombstoneEvents(int count, String sourceTable, String servers, String schemaRegistryUrl) {
        var producer = buildAvroProducer(servers, schemaRegistryUrl);

        for(int i=0; i < count; i++) {
            String txIdAsString = "-1";
            var avroVal = buildTxAvroPayload(txIdAsString, "", "d");
            var avroKey = buildTxAvroKey(txIdAsString, "1594733209187");

            ProducerRecord<GenericRecord, GenericRecord> record = new ProducerRecord<>(sourceTopicOltp1, avroKey, avroVal);

            record.headers().add("__source_table", sourceTable.getBytes(StandardCharsets.UTF_8));
            record.headers().add("__source_schema", "dbo".getBytes(StandardCharsets.UTF_8));
            record.headers().add("__source_db", "online".getBytes(StandardCharsets.UTF_8));
            producer.send(record);
        }

        producer.flush();
        producer.close();
    }

    @Test
    public void testFilteringFlow() throws Exception {
        String servers = String.join(",", properties.getBootstrapServers());
        String schemaRegistryUrl = properties.getProperties().get("schema.registry.url");
        System.out.printf("Kafka Container: %s%n \n", servers);
        System.out.println("Schema Registry url =" + schemaRegistryUrl);

        loadConfigurationTable(servers, schemaRegistryUrl);

        // produce some valid records
        long txMainId = 4110000000000000001L;
        produceTxEvents(3, txMainId, "TransactionMain", servers, schemaRegistryUrl);

        // produce some invalid records
        txMainId = 2110000000000000001L;
        produceTxEvents(3, txMainId, "TransactionMain", servers, schemaRegistryUrl);

        // produce some events with the wrong table but with good id
        txMainId = 4110000000000000005L;
        produceTxEvents(3, txMainId, "ThisIsAWrongTable", servers, schemaRegistryUrl);

        produceTombstoneEvents(3, "TransactionMain", servers, schemaRegistryUrl);

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

        int numOfRecords = 9;

        try {
            Awaitility.await().atMost(10, SECONDS)
                    .until(() -> values.size() == numOfRecords);
            assertThat(values).hasSize(numOfRecords);

            List<GenericRecord> tombstones = values.stream().filter(Objects::isNull).collect(Collectors.toList());
            List<GenericRecord> nonTombstones = values.stream().filter(Objects::nonNull).collect(Collectors.toList());

            assertThat(tombstones).hasSize(3);
            assertThat(nonTombstones).hasSize(6);

            for(GenericRecord record : nonTombstones) {
                var txIdString = record.get("TransactionMainID").toString();
                assertThat(Long.valueOf(txIdString)).is(new Condition<>() {
                    @Override
                    public boolean matches(Long value) {
                        return (value == -1) || ((4110000000000000001L <= value) && (value <= 4110000000000000003L));
                    }
                });
            }
        } finally {
            consumingTask.cancel(true);
            service.awaitTermination(100, MILLISECONDS);
        }
    }
}