package com.purbon.streams.ks.topologies;

import com.purbon.streams.ks.model.envelopes.MessageImpl;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;

public interface TopologyBuilder<K, V> {

    KStream<K, MessageImpl<V>> build(StreamsBuilder builder);
}
