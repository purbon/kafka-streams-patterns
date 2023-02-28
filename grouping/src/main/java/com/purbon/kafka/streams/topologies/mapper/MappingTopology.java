package com.purbon.kafka.streams.topologies.mapper;

import com.purbon.kafka.streams.model.Flight;
import com.purbon.kafka.streams.model.FlightKey;
import com.purbon.kafka.streams.model.Passenger;
import com.purbon.kafka.streams.model.PassengerKey;
import com.purbon.kafka.streams.model.Passengers;
import com.purbon.kafka.streams.serdes.JSONSerde;
import lombok.Getter;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KeyValueMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.regex.Pattern;

@Getter
public class MappingTopology {

    private final KStream<FlightKey, Flight> stream;
    Logger logger = LoggerFactory.getLogger(MappingTopology.class);

    public static class Builder {

        Logger logger = LoggerFactory.getLogger(MappingTopology.Builder.class);

        private Pattern topicPattern;
        private Map<String, String> serdeConfig;

        private KStream<FlightKey, Flight> stream;

        public MappingTopology build(StreamsBuilder builder) {
            final Serde<FlightKey> flightKeySerde = serde(true, FlightKey.class);
            final Serde<Flight> flightValueSerde = serde(true, Flight.class);
            final Serde<PassengerKey> passengerKeySerde = serde(true, PassengerKey.class);
            final Serde<Passenger> passengerSerde = serde(true, Passenger.class);
            final Serde<Passengers> passengersSerde = serde(true, Passengers.class);


            builder.stream("flights", Consumed.with(flightKeySerde, flightValueSerde))
                    .map(new KeyValueMapper<FlightKey, Flight, KeyValue<?, ?>>() {
                        @Override
                        public KeyValue<?, ?> apply(FlightKey key, Flight value) {
                            return null;
                        }
                    })
                    .to("mapped.flights");

            return new MappingTopology(this);
        }

        public Builder setTopicPattern(Pattern topicPattern) {
            this.topicPattern = topicPattern;
            return this;
        }

        public Builder setSerdeConfig(Map<String, String> serdeConfig) {
            this.serdeConfig = serdeConfig;
            return this;
        }

        private <T> Serde<T> serde(boolean isKey, Class<T> myClass) {
            var jsonSerde = new JSONSerde<T>(myClass);
            jsonSerde.configure(serdeConfig, isKey);
            return jsonSerde;
        }

    }

    private MappingTopology(Builder builder) {
        stream = builder.stream;
    }

}
