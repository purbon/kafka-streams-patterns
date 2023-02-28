package com.purbon.kafka.streams.topologies;

import com.purbon.kafka.streams.model.Transaction;
import com.purbon.kafka.streams.serdes.CustomSerdes;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.state.Stores;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.config.KafkaStreamsInfrastructureCustomizer;

import java.util.Map;

import static com.purbon.kafka.streams.topologies.AccumulateProcessor.STATE_STORE_NAME;
import static com.purbon.kafka.streams.topologies.TransactionTopologyBuilder.TRANSACTION_TOPIC;

public class DelayedTxInfraCustomizer implements KafkaStreamsInfrastructureCustomizer {

    private final Serde<Transaction> txSerdes;
    public static final String DELAYED_TRANSACTION_TOPIC = "delayed.transactions";
    private Topology topology;

    public DelayedTxInfraCustomizer(CustomSerdes serdes, Map<String, ?> config) {
        txSerdes = serdes.transactionSerde(config, false);
    }

    @Override
    public void configureBuilder(@Qualifier("delayedConfigTopologyConfig") StreamsBuilder builder) {
        var storeBuilder =
                Stores.keyValueStoreBuilder(
                        Stores.persistentKeyValueStore(STATE_STORE_NAME),
                        Serdes.Long(),
                        txSerdes
                        );


       topology = builder.build(); // PAPI
       topology.addSource("Source", Serdes.Long().deserializer(), txSerdes.deserializer(), DELAYED_TRANSACTION_TOPIC)
               .addProcessor("DelayedProcess", AccumulateProcessor::new, "Source")
               .addStateStore(storeBuilder, "DelayedProcess")
               .addSink("Sink", TRANSACTION_TOPIC, Serdes.Long().serializer(), txSerdes.serializer(), "DelayedProcess");
    }

    public Topology getLatestTopology() {
        return topology;
    }
}
