package com.purbon.kafka.streams.topologies;

import io.confluent.kafka.streams.serdes.avro.GenericAvroSerde;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.ValueMapper;
import org.apache.kafka.streams.processor.TopicNameExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Pattern;

public class FilterTopology {

    Logger logger = LoggerFactory.getLogger(FilterTopology.class);

    public static final String KEY_ID = "keyId";
    public static final String MIN_COL = "minColumn";
    public static final String MAX_COL = "maxColumn";
    public static final String KEY_AVRO_SCHEMA = "keyAvroSchema";
    public static final String KEY_SCHEMA_DB_FIELD = "keySchemaDbField";
    public static final String KEY_SCHEMA_SCHEMA_FIELD = "keySchemaSchemaField";
    public static final String KEY_SCHEMA_TABLE_FIELD = "keySchemaTableField";

    public static class Builder {
        Logger logger = LoggerFactory.getLogger(FilterTopology.Builder.class);

        private Pattern topicPattern;
        private String configurationTableName;
        private Map<String, String> serdeConfig;
        private Map<String, String> filterConfig;
        private TopicNameExtractor<GenericRecord, GenericRecord> extractor;

        public FilterTopology build(StreamsBuilder builder) {
            final Serde<GenericRecord> keyAvroSerde = serde(true);
            final Serde<GenericRecord> valueAvroSerde = serde(false);

            var gTable = builder.globalTable(configurationTableName,
                    Consumed.with(keyAvroSerde, valueAvroSerde));

            var tableEntries = builder.stream(topicPattern, Consumed.with(keyAvroSerde, valueAvroSerde));

            var enrichedTable = tableEntries
                    .transform(() -> new TableNameMapperTransformer(filterConfig))
                    .peek((key, value) ->  logger.debug("[BeforeJoin] key= "+key+ " value="+value))
                    .join(gTable,
                            (k, v) -> k.left, // Tuple.left (__source_table) == GlobalKTable.key
                            (left, right) -> {
                                var tuple = new Tuple<GenericRecord, GenericRecord>();
                                tuple.left = left; // value (GenericRecord) from tableEntries
                                tuple.right = right; // value (GenericRecord) from globalTable (config table)
                                return tuple;
                            })
                    .peek((key, value) -> logger.debug("[AfterJoin] key= "+key+ " value="+value));


            var filteredData = enrichedTable
                    .filter((key, value) -> {
                        GenericRecord filterConfigTable = value.right;
                        GenericRecord valueTable = value.left;
                        try {
                            var keyId = filterConfigTable.get(filterConfig.get(KEY_ID)).toString();
                            var minVal = Long.valueOf(filterConfigTable.get(filterConfig.get(MIN_COL)).toString());
                            var maxVal = Long.valueOf(filterConfigTable.get(filterConfig.get(MAX_COL)).toString());

                            var valueId = Long.valueOf(valueTable.get(keyId).toString());
                            return (isDeleteRecord(valueTable)) || ((minVal <= valueId) && (valueId <= maxVal));
                        } catch (NullPointerException ex) {
                            logger.debug("Some of the values returned a null", ex);
                            return true;
                        }
                    })
                    .map((key, value) -> KeyValue.pair(key.right, value.left))
                    .flatMapValues((ValueMapper<GenericRecord, Iterable<GenericRecord>>) value -> {
                        Object operation = value.get("__op");
                        if (operation != null && String.valueOf(operation).equalsIgnoreCase("d")) {
                            return new ArrayList<>(Arrays.asList(value, null));
                        } else {
                            return new ArrayList<>(Collections.singletonList(value));
                        }
                    });

            filteredData
                    .to(extractor, Produced.with(keyAvroSerde, valueAvroSerde));

            return new FilterTopology(filteredData);

        }

        private boolean isDeleteRecord(GenericRecord record) {
            try {
                String operationField = "__op";
                if (record.hasField(operationField)) {
                    var obj = record.get("__op");
                    return String.valueOf(obj).equalsIgnoreCase("d");
                } else {
                    return false;
                }
            } catch (NullPointerException ex) {
                logger.debug("Are the record being filtered proper Debezium ones, no operation field is found", ex);
                return false;
            }
        }

        private Serde<GenericRecord> serde(boolean isKey) {
            Serde<GenericRecord> avroSerde = new GenericAvroSerde();
            avroSerde.configure(serdeConfig, isKey);
            return avroSerde;
        }

        public Builder setTopicPattern(Pattern topicPattern) {
            this.topicPattern = topicPattern;
            return this;
        }

        public Builder setConfigurationTableName(String configurationTableName) {
            this.configurationTableName = configurationTableName;
            return this;
        }

        public Builder setSerdeConfig(Map<String, String> serdeConfig) {
            this.serdeConfig = serdeConfig;
            return this;
        }

        public Builder setFilterConfig(Map<String, String> filterConfig) {
            this.filterConfig = filterConfig;
            return this;
        }

        public Builder setExtractor(TopicNameExtractor<GenericRecord, GenericRecord> extractor) {
            this.extractor = extractor;
            return this;
        }
    }

    private KStream<GenericRecord, GenericRecord> stream;

    private FilterTopology(KStream<GenericRecord, GenericRecord> stream) {
        this.stream = stream;
    }

    public KStream<GenericRecord, GenericRecord> getStream() {
        return stream;
    }
}
