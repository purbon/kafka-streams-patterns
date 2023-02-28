package com.purbon.kafka.streams.topologies;

import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;

public interface TopologyBuilder<K, V> {

    KStream<K, V> build(StreamsBuilder builder);
}
