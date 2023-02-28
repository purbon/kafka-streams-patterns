package com.purbon.kafka.streams;

import com.purbon.kafka.streams.model.Flight;
import com.purbon.kafka.streams.model.FlightKey;
import com.purbon.kafka.streams.model.Passenger;
import com.purbon.kafka.streams.model.PassengerKey;
import com.purbon.kafka.streams.model.Passengers;
import com.purbon.kafka.streams.serdes.JSONSerde;
import com.purbon.kafka.streams.topologies.grouping.GroupingTopology;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.TopologyTestDriver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class GroupingTopologyTest {

    private TopologyTestDriver testDriver;
    private TestInputTopic<FlightKey, Flight> flightsTopic;
    private TestInputTopic<PassengerKey, Passenger> passengerTopic;
    private TestOutputTopic<FlightKey, Flight> enrichtedTopic;

    @BeforeEach
    public void before() {

        final Serde<FlightKey> flightKeySerde = serde(true, FlightKey.class);
        final Serde<Flight> flightValueSerde = serde(true, Flight.class);
        final Serde<PassengerKey> passengerKeySerde = serde(true, PassengerKey.class);
        final Serde<Passenger> passengerSerde = serde(true, Passenger.class);
        final Serde<Passengers> passengersSerde = serde(true, Passengers.class);


        StreamsBuilder streamsBuilder = new StreamsBuilder();
        new GroupingTopology.Builder()
                .setSerdeConfig(new HashMap<>())
                .build(streamsBuilder);

        Properties config = new Properties();
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "testGroup");
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234");
        config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        testDriver = new TopologyTestDriver(streamsBuilder.build(), config);

        flightsTopic = testDriver.createInputTopic(
                "flights",
                flightKeySerde.serializer(),
                flightValueSerde.serializer());

        passengerTopic = testDriver.createInputTopic(
                "passengers",
                passengerKeySerde.serializer(),
                passengerSerde.serializer());

        enrichtedTopic = testDriver.createOutputTopic(
                "enriched.flights",
                flightKeySerde.deserializer(),
                flightValueSerde.deserializer());


    }

    @AfterEach
    public void after() {
        testDriver.close();
    }

    @Test
    public void testAggJoin() {

        var flightId = "1";
        var aMarkOfTime = new Timestamp(System.currentTimeMillis());

        FlightKey flightKey = new FlightKey(flightId, aMarkOfTime);
        Flight flight = new Flight(flightId, aMarkOfTime, "Barcelona", "Berlin", new Passengers(new ArrayList<>()));

        for(int i=0; i < 5; i++) {
            PassengerKey passengerKey = new PassengerKey(String.valueOf(i));
            Passenger passenger = new Passenger(String.valueOf(i), "PersonName "+i, "BCN", flightId, aMarkOfTime);

            passengerTopic.pipeInput(passengerKey, passenger);
        }
        flightsTopic.pipeInput(flightKey, flight);

        assertThat(enrichtedTopic.getQueueSize()).isEqualTo(1);
        var readFlight= enrichtedTopic.readValue();
        assertThat(readFlight.getPassengers().getList()).isNotNull();
        assertThat(readFlight.getPassengers().getList()).asList().hasSize(5);

    }

    private <T> Serde<T> serde(boolean isKey, Class<T> myClass) {
        var jsonSerde = new JSONSerde<T>(myClass);
        jsonSerde.configure(new HashMap<>(), isKey);
        return jsonSerde;
    }
}
