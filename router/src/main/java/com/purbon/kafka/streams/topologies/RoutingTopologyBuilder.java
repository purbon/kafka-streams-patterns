package com.purbon.kafka.streams.topologies;

import com.purbon.kafka.streams.services.RoutingService;
import io.confluent.kafka.streams.serdes.avro.GenericAvroSerde;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.processor.TopicNameExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;
import java.util.regex.Pattern;

public class RoutingTopologyBuilder implements TopologyBuilder<GenericRecord, GenericRecord>  {

    Logger logger = LoggerFactory.getLogger(RoutingTopologyBuilder.class);

    private Pattern topicPattern;
    private Map<String, String> serdeConfig;
    private TopicNameExtractor<GenericRecord, GenericRecord> extractor;

    public RoutingTopologyBuilder(String topicPattern,
                                  TopicNameExtractor<GenericRecord, GenericRecord> extractor,
                                  Map<String, String> serdeConfig) {
        this.topicPattern =  Pattern.compile(topicPattern);
        this.extractor = extractor;
        this.serdeConfig = serdeConfig;
    }

    @Override
    public KStream<GenericRecord, GenericRecord> build(StreamsBuilder builder) {
        final Serde<GenericRecord> keyAvroSerde = serde(true);
        final Serde<GenericRecord> valueAvroSerde = serde(false);

        var mySourceStream = builder
                .stream(topicPattern, Consumed.with(keyAvroSerde, valueAvroSerde));
        mySourceStream
                .peek((key, value) -> logger.debug("RoutingTopologyBuilder: key="+key+" value="+value))
                .to(extractor, Produced.with(keyAvroSerde, valueAvroSerde));

        return mySourceStream;
    }

    private Serde<GenericRecord> serde(boolean isKey) {
        Serde<GenericRecord> avroSerde = new GenericAvroSerde();
        avroSerde.configure(serdeConfig, isKey);
        return avroSerde;
    }

}
