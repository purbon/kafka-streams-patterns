package com.purbon.kafka.streams;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.processor.StateStore;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.ProcessorSupplier;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.Stores;
import org.springframework.kafka.config.KafkaStreamsInfrastructureCustomizer;

public class AddGlobalStoreCustomizer implements KafkaStreamsInfrastructureCustomizer {

    @Override
    public void configureBuilder(StreamsBuilder builder) {
        var storeBuilder =
                Stores.keyValueStoreBuilder(
                        Stores.persistentKeyValueStore("mapping"), //applicationId.stateStoreName-changelog
                        Serdes.String(),
                        Serdes.String()
                );
        var store = storeBuilder.build();


        builder.addGlobalStore(storeBuilder, "foo", Consumed.with(Serdes.String(), Serdes.String()), new ProcessorSupplier<String, String, Void, Void>() {
            @Override
            public Processor<String, String, Void, Void> get() {
                return new Processor<String, String, Void, Void>() {

                    ProcessorContext<Void, Void> context;
                    KeyValueStore<String, String> store;

                    @Override
                    public void init(ProcessorContext<Void, Void> context) {
                        this.context = context;
                        this.store = context.getStateStore("mapping");

                    }

                    @Override
                    public void process(Record<String, String> record) {
                        this.store.put(record.key(), record.value());
                    }
                };
            }
        });
    }
}
