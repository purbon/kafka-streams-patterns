package com.purbon.kafka.streams.topologies;

import lombok.Getter;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@Getter
public class WireUpTopology {

    private final KStream<String, String> stream;
    Logger logger = LoggerFactory.getLogger(WireUpTopology.class);

    public static class Builder {

        Logger logger = LoggerFactory.getLogger(WireUpTopology.Builder.class);

        private String sourceTopic;
        private String targetTopic;
        private Map<String, String> serdeConfig;

        private KStream<String, String> stream;

        public WireUpTopology build(StreamsBuilder builder) {

            this.stream = builder.stream(sourceTopic, Consumed.with(Serdes.String(), Serdes.String()));
            stream.to(targetTopic);

            return new WireUpTopology(this);
        }

        public Builder setSourceTopic(String sourceTopic) {
            this.sourceTopic = sourceTopic;
            return this;
        }

        public Builder setTargetTopic(String targetTopic) {
            this.targetTopic = targetTopic;
            return this;
        }

        public Builder setSerdeConfig(Map<String, String> serdeConfig) {
            this.serdeConfig = serdeConfig;
            return this;
        }

    }

    private WireUpTopology(Builder builder) {
        stream = builder.stream;
    }

}
