package com.purbon.kafka.streams.topologies;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import io.swagger.util.Json;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Aggregator;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Initializer;
import org.apache.kafka.streams.kstream.JoinWindows;
import org.apache.kafka.streams.kstream.Joined;
import org.apache.kafka.streams.kstream.KGroupedStream;
import org.apache.kafka.streams.kstream.KeyValueMapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.purbon.kafka.streams.serdes.*;
import org.apache.kafka.streams.kstream.Printed;
import org.apache.kafka.streams.kstream.Reducer;
import org.apache.kafka.streams.kstream.ValueJoiner;

public class FlightInfo {
    public static Properties config() {
        final Properties config = new Properties();
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "flights");
        config.put(StreamsConfig.CLIENT_ID_CONFIG, "client");

        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9093");
        config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());

        config.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 100);
        config.put(StreamsConfig.NUM_STREAM_THREADS_CONFIG, 4);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return config;
    }

    public static void main(String[] args) throws Exception {

        Serde<JsonNode> keySerde = jsonSerde(true);
        Serde<JsonNode> valueSerde = jsonSerde(false);

        StreamsBuilder builder = new StreamsBuilder();

        var passangers = builder.stream("local.dbo.PassangersInfo", Consumed.with(keySerde, valueSerde))
                .groupBy(new KeyValueMapper<JsonNode, JsonNode, JsonNode>() {

                    @Override
                    public JsonNode apply(JsonNode key, JsonNode value) {
                        ObjectNode node = JsonNodeFactory.instance.objectNode();
                        node.set("flightId", value.get("flightId"));
                        node.set("flightDate", value.get("flightDate"));
                        return node;
                    }
                }).aggregate(new Initializer<List<JsonNode>>() {
                    @Override
                    public List<JsonNode> apply() {
                        return new ArrayList<>();
                    }
                }, new Aggregator<JsonNode, JsonNode, List<JsonNode>>() {
                    @Override
                    public List<JsonNode> apply(JsonNode key, JsonNode value, List<JsonNode> aggregate) {
                        aggregate.add(value);
                        return aggregate;
                    }
                });

        var flights = builder.stream("local.dbo.FlightInfo", Consumed.with(keySerde, valueSerde));


        var result = flights.join(passangers, new ValueJoiner<JsonNode, List<JsonNode>, JsonNode>() {
            @Override
            public JsonNode apply(JsonNode value1, List<JsonNode> value2) {
                return null;
            }
        });

        result.print(Printed.toSysOut());


    }

//	{
//		  "passangerId": 3,
//		  "firstName": "Yonatan",
//		  "lastName": "Trabelsi",
//		  "flightId": 1,
//		  "flightDate": "1644837600000000000",
//		  "__op": "c",
//		  "__deleted": "false"
//		}

    private static Serde<JsonNode> jsonSerde(boolean isKey) {
        var serde = Serdes.serdeFrom(new JsonSerializer(), new JsonDeserializer());
        serde.configure(Collections.emptyMap(), isKey);
        return serde;
    }
}
