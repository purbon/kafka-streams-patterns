package com.purbon.kafka.streams.topologies.grouping;

import com.purbon.kafka.streams.model.Flight;
import com.purbon.kafka.streams.model.FlightKey;
import com.purbon.kafka.streams.model.Passenger;
import com.purbon.kafka.streams.model.PassengerKey;
import com.purbon.kafka.streams.model.Passengers;
import com.purbon.kafka.streams.serdes.JSONSerde;
import lombok.Getter;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Joined;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KeyValueMapper;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Printed;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.Serialized;
import org.apache.kafka.streams.kstream.ValueJoiner;
import org.apache.kafka.streams.state.KeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Pattern;

import static org.apache.kafka.streams.kstream.Materialized.as;

@Getter
public class GroupingTopology {

    private final KStream<FlightKey, Flight> stream;
    Logger logger = LoggerFactory.getLogger(GroupingTopology.class);

    public static class Builder {

        Logger logger = LoggerFactory.getLogger(GroupingTopology.Builder.class);

        private Pattern topicPattern;
        private Map<String, String> serdeConfig;

        private KStream<FlightKey, Flight> stream;

        public GroupingTopology build(StreamsBuilder builder) {
            final Serde<FlightKey> flightKeySerde = serde(true, FlightKey.class);
            final Serde<Flight> flightValueSerde = serde(true, Flight.class);
            final Serde<PassengerKey> passengerKeySerde = serde(true, PassengerKey.class);
            final Serde<Passenger> passengerSerde = serde(true, Passenger.class);
            final Serde<Passengers> passengersSerde = serde(true, Passengers.class);

            var materialized = Materialized.<FlightKey, Passengers, KeyValueStore<Bytes, byte[]>>
                    as("Agg-Materialized")
                    .withValueSerde(passengersSerde)
                    .withKeySerde(flightKeySerde);


            var passengersStream = builder.stream("passengers", Consumed.with(passengerKeySerde, passengerSerde))
                   .groupBy(new KeyValueMapper<PassengerKey, Passenger, FlightKey>() {
                        @Override
                        public FlightKey apply(PassengerKey key, Passenger value) {
                            return new FlightKey(value.getFlightId(), value.getFlightDate());
                        }
                    }, Serialized.with(flightKeySerde, passengerSerde))
                   .aggregate(() -> new Passengers(new ArrayList<>()),
                            (flightKey, passenger, aggregate) -> {
                                aggregate.getList().add(passenger);
                                return aggregate;
                            }, materialized);

            passengersStream.toStream().print(Printed.toSysOut());

            var flightsStream = builder.stream("flights", Consumed.with(flightKeySerde, flightValueSerde));

            stream = flightsStream.join(passengersStream, new ValueJoiner<Flight, Passengers, Flight>() {
                @Override
                public Flight apply(Flight flight, Passengers passengers) {
                    flight.setPassengers(passengers);
                    return flight;
                }
            }, Joined.with(flightKeySerde, flightValueSerde, passengersSerde));

            stream.to("enriched.flights", Produced.with(flightKeySerde, flightValueSerde));

            return new GroupingTopology(this);
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

    private GroupingTopology(Builder builder) {
        stream = builder.stream;
    }

}
