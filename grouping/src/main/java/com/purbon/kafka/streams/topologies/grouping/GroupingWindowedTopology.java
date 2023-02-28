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
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Aggregator;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.Initializer;
import org.apache.kafka.streams.kstream.JoinWindows;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KeyValueMapper;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Printed;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.StreamJoined;
import org.apache.kafka.streams.kstream.Suppressed;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.kstream.ValueJoiner;
import org.apache.kafka.streams.kstream.Windowed;
import org.apache.kafka.streams.state.WindowStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Pattern;

import static org.apache.kafka.streams.kstream.Suppressed.BufferConfig.unbounded;

@Getter
public class GroupingWindowedTopology {

    private final KStream<FlightKey, Flight> stream;
    Logger logger = LoggerFactory.getLogger(GroupingWindowedTopology.class);

    public static class Builder {

        Logger logger = LoggerFactory.getLogger(GroupingWindowedTopology.Builder.class);

        private Pattern topicPattern;
        private Map<String, String> serdeConfig;

        private KStream<FlightKey, Flight> stream;

        public GroupingWindowedTopology build(StreamsBuilder builder) {
            final Serde<FlightKey> flightKeySerde = serde(true, FlightKey.class);
            final Serde<Flight> flightValueSerde = serde(true, Flight.class);
            final Serde<PassengerKey> passengerKeySerde = serde(true, PassengerKey.class);
            final Serde<Passenger> passengerSerde = serde(true, Passenger.class);
            final Serde<Passengers> passengersSerde = serde(true, Passengers.class);

            var windowedMaterialized = Materialized.<FlightKey, Passengers, WindowStore<Bytes, byte[]>>
                    as("Agg-Windowed-Materialized")
                    .withCachingDisabled()
                    .withValueSerde(passengersSerde)
                    .withKeySerde(flightKeySerde);

            var timeWindow = TimeWindows
                    .of(Duration.ofMinutes(10))
                    .grace(Duration.ofMillis(0));

            var passengersTable = builder
                    .stream("passengers", Consumed.with(passengerKeySerde, passengerSerde))
                   .groupBy(new KeyValueMapper<PassengerKey, Passenger, FlightKey>() {
                        @Override
                        public FlightKey apply(PassengerKey key, Passenger value) {
                            return new FlightKey(value.getFlightId(), value.getFlightDate());
                        }
                    }, Grouped.with(flightKeySerde, passengerSerde))
                    .windowedBy(timeWindow)
                    .aggregate(new Initializer<Passengers>() {
                        @Override
                        public Passengers apply() {
                            return new Passengers(new ArrayList<>());
                        }
                    }, new Aggregator<FlightKey, Passenger, Passengers>() {
                        @Override
                        public Passengers apply(FlightKey key, Passenger passsenger, Passengers passengers) {
                            passengers.getList().add(passsenger);
                            return passengers;
                        }
                    }, windowedMaterialized)
                    .suppress(Suppressed.untilWindowCloses(unbounded()));

            passengersTable.toStream().print(Printed.toSysOut());

            var passengersStream = passengersTable
                    .toStream()
                    .map(new KeyValueMapper<Windowed<FlightKey>, Passengers, KeyValue<FlightKey, Passengers>>() {
                        @Override
                        public KeyValue<FlightKey, Passengers> apply(Windowed<FlightKey> key, Passengers value) {
                            var callTimestap = new Timestamp(System.currentTimeMillis());
                            System.out.println("****");
                            System.out.println(key.window().startTime()+" - "+key.window().endTime() + " "+callTimestap);
                            System.out.println("****");

                            System.out.println("Map"+ value.getList().size());
                            return KeyValue.pair(key.key(), value);
                        }
                    });

            var flightsStream = builder.stream("flights", Consumed.with(flightKeySerde, flightValueSerde));

            stream = flightsStream.join(passengersStream, new ValueJoiner<Flight, Passengers, Flight>() {
                @Override
                public Flight apply(Flight flight, Passengers passengers) {
                    flight.setPassengers(passengers);
                    return flight;
                }
            }, JoinWindows.of(Duration.ofHours(1)), StreamJoined.with(flightKeySerde, flightValueSerde, passengersSerde));
            stream.to("enriched.flights", Produced.with(flightKeySerde, flightValueSerde));
            return new GroupingWindowedTopology(this);
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

    private GroupingWindowedTopology(Builder builder) {
        stream = builder.stream;
    }

}
