package com.purbon.kafka.streams;

import com.purbon.kafka.streams.topologies.mapper.MapperProcessor;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.ProcessorSupplier;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.Stores;
import org.springframework.kafka.config.KafkaStreamsInfrastructureCustomizer;

public class MappingTopologyCustomizer implements KafkaStreamsInfrastructureCustomizer {

    private final String SOURCE_TOPIC = "foo";
    private final String TARGET_TOPIC = "bar";

    private Topology topology;

    @Override
    public void configureBuilder(StreamsBuilder builder) {
        var storeBuilder =
                Stores.keyValueStoreBuilder(
                        Stores.persistentKeyValueStore("mapping"), //applicationId.stateStoreName-changelog
                        Serdes.String(),
                        Serdes.String()
                );
        var store = storeBuilder.build();

        topology = builder.build(); // PAPI
        topology.addSource("Source", Serdes.String().deserializer(), Serdes.String().deserializer(), SOURCE_TOPIC)
                .addProcessor("mapper", MapperProcessor::new, "Source")
                //.addStateStore(storeBuilder, "mapper")
                .addGlobalStore(storeBuilder, "mapper",
                        Serdes.String().deserializer(), Serdes.String().deserializer(),
                        "zet",
                        "processor",
                        new ProcessorSupplier<String, String, Void, Void>() {
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
                        })
                .addSink("Sink", TARGET_TOPIC, Serdes.String().serializer(), Serdes.String().serializer(), "mapper");

    }
}
