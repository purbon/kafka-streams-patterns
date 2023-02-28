package com.purbon.kafka.streams;

import com.purbon.kafka.streams.model.Flight;
import com.purbon.kafka.streams.model.FlightKey;
import com.purbon.kafka.streams.model.Passenger;
import com.purbon.kafka.streams.model.PassengerKey;
import com.purbon.kafka.streams.model.Passengers;
import com.purbon.kafka.streams.serdes.JSONSerde;
import com.purbon.kafka.streams.topologies.grouping.GroupingWindowedTopology;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.kstream.internals.TimeWindow;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class GroupingWindowedTopologyTest {

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
        new GroupingWindowedTopology.Builder()
                .setSerdeConfig(new HashMap<>())
                .build(streamsBuilder);

        Properties config = new Properties();
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "testGroupWindow");
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234");
        config.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);
        config.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 100);
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
    public void testAggJoin() throws InterruptedException {

        var flightId = String.valueOf("1");
        var longAsTime = System.currentTimeMillis();
        var aMarkOfTime = new Timestamp(longAsTime);

        System.out.println("Adding flight event with timestamp: "+longAsTime);

        FlightKey flightKey = new FlightKey(flightId, aMarkOfTime);
        Flight flight = new Flight(flightId, aMarkOfTime, "Barcelona", "Berlin", new Passengers(new ArrayList<>()));

        flightsTopic.pipeInput(flightKey, flight, longAsTime);

        int eventsCount = 5;
        long increment = 5000 / eventsCount;

        for (int i = 0; i < eventsCount; i++) {
            PassengerKey passengerKey = new PassengerKey(String.valueOf(i));
            Passenger passenger = new Passenger(String.valueOf(i), "PersonName " + i, "BCN", flightId, aMarkOfTime);

            long eventTime = longAsTime + (increment * i);
            //System.out.println("adding passenger "+i+" at: "+eventTime + " with a diff of: "+(eventTime-longAsTime));

            passengerTopic.pipeInput(passengerKey, passenger, eventTime);
        }

        int i= eventsCount + 1;
        PassengerKey passengerKey = new PassengerKey(String.valueOf(i));
        Passenger passenger = new Passenger(String.valueOf(i), "PersonName " + i, "BCN", flightId, aMarkOfTime);

        long eventTime = longAsTime + (61*60*1000);
        passengerTopic.pipeInput(passengerKey, passenger, eventTime);
/*
        var store = testDriver.getWindowStore("Agg-Windowed-Materialized");
        var records = store.fetchAll(longAsTime-1000000000, longAsTime+1000000000);
        while (records.hasNext()) {
            var next = records.next();
            System.out.println(next);
        }
*/
        assertThat(enrichtedTopic.getQueueSize()).isEqualTo(1);

        var readFlight = enrichtedTopic.readValue();
        assertThat(readFlight.getPassengers().getList()).isNotNull();
        assertThat(readFlight.getPassengers().getList()).asList().hasSize(eventsCount);
    }

    @Test
    public void timeWindowTest() {

        var windows = TimeWindows.of(Duration.ofMinutes(10));
        var timestamp = 1644934321782L; // Tuesday, 15 February 2022 14:12:01.782 GMT

        var map = windows.windowsFor(timestamp);

        for(TimeWindow window : map.values()) {
            System.out.println(window.startTime()+ " " + window.endTime());
        }
    }

    private <T> Serde<T> serde(boolean isKey, Class<T> myClass) {
        var jsonSerde = new JSONSerde<T>(myClass);
        jsonSerde.configure(new HashMap<>(), isKey);
        return jsonSerde;
    }
}
