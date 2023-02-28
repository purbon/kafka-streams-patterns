package com.purbon.kafka.streams.topologies;

import io.confluent.kafka.streams.serdes.avro.GenericAvroSerde;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.processor.TopicNameExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.regex.Pattern;

@Getter
public class RoutingTopology {

    private final KStream<GenericRecord, GenericRecord> stream;
    Logger logger = LoggerFactory.getLogger(RoutingTopology.class);

    public static class Builder {

        Logger logger = LoggerFactory.getLogger(RoutingTopology.Builder.class);

        private Pattern topicPattern;
        private Map<String, String> serdeConfig;
        private TopicNameExtractor<GenericRecord, GenericRecord> extractor;

        private KStream<GenericRecord, GenericRecord> stream;

        public RoutingTopology build(StreamsBuilder builder) {
            final Serde<GenericRecord> keyAvroSerde = serde(true);
            final Serde<GenericRecord> valueAvroSerde = serde(false);

            logger.debug("RoutingTopologyBuilder: topicPattern ->"+topicPattern);

            stream = builder
                    .stream(topicPattern, Consumed.with(keyAvroSerde, valueAvroSerde))
                    .trans

            var stream2 = null;
            var stream3 = null;


            stream.leftJoin(stream2)

            stream
                    .peek((key, value) -> logger.debug("RoutingTopologyBuilder: key="+key+" value="+value))
                    .to(extractor, Produced.with(keyAvroSerde, valueAvroSerde));

            return new RoutingTopology(this);
        }


        public Builder setTopicPattern(Pattern topicPattern) {
            this.topicPattern = topicPattern;
            return this;
        }

        public Builder setSerdeConfig(Map<String, String> serdeConfig) {
            this.serdeConfig = serdeConfig;
            return this;
        }

        public Builder setExtractor(TopicNameExtractor<GenericRecord, GenericRecord> extractor) {
            this.extractor = extractor;
            return this;
        }

        private Serde<GenericRecord> serde(boolean isKey) {
            Serde<GenericRecord> avroSerde = new GenericAvroSerde();
            avroSerde.configure(serdeConfig, isKey);
            return avroSerde;
        }

    }

    private RoutingTopology(Builder builder) {
        stream = builder.stream;
    }

}
