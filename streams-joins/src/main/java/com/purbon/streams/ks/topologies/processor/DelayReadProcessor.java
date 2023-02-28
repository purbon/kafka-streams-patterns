package com.purbon.streams.ks.topologies.processor;

import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.PunctuationType;
import org.apache.kafka.streams.processor.Punctuator;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.KeyValueStore;

import java.time.Duration;

public class DelayReadProcessor<K, V> implements Processor<K, V> {

    private ProcessorContext context;
    private KeyValueStore store;

    private long pullingInSeconds;

    public DelayReadProcessor(long pullingInSeconds) {
        this.pullingInSeconds = pullingInSeconds;
    }

    @Override
    public void init(ProcessorContext context) {
        this.context = context;
        this.store = context.getStateStore("dlq.store");

        this.context.schedule(Duration.ofSeconds(pullingInSeconds),
                PunctuationType.STREAM_TIME,
                timestamp -> {
                    KeyValueIterator<K, V> iter = store.all();
                    while(iter.hasNext()) {
                        KeyValue<K, V> entry = iter.next();
                        context.forward(entry.key, entry.value);
                    }

                });

        this.context.schedule(Duration.ofSeconds(42), PunctuationType.WALL_CLOCK_TIME,
                new Punctuator() {
                    @Override
                    public void punctuate(long timestamp) {
                        store.all();
                    }
                });
    }

    @Override
    public void process(K key, V value) {
        this.store.put(key, value);
    }

    @Override
    public void close() {

    }
}
