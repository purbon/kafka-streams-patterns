package com.purbon.kafka.streams.utils;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;

import java.util.Properties;

public class App {

    public static void main(String[] args) {

        Properties props = new Properties();
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29092");
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "app");
        props.put("consumer.request.timeout.ms", 10);
        props.put("producer.request.timeout.ms", 10);
        props.put("admin.request.timeout.ms", 10);

        StreamsBuilder sb = new StreamsBuilder();
        sb.stream("foo")
          .to("bar");

        var streams = new KafkaStreams(sb.build(), props);

        streams.start();

        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));


    }
}
