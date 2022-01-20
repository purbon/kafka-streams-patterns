package com.purbon.kafka.streams.topologies;

import com.purbon.kafka.streams.model.Transaction;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.processor.PunctuationType;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.KeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

public class AccumulateProcessor implements Processor<Long, Transaction, Long, Transaction> {

    public static final String STATE_STORE_NAME = "accumulateStore";
    private KeyValueStore<Long, Transaction> store;

    private Logger logger = LoggerFactory.getLogger(AccumulateProcessor.class);


    @Override
    public void init(ProcessorContext<Long, Transaction> context) {

        System.out.println(context.getClass());
        System.out.println(context.valueSerde());

        store = context.getStateStore(STATE_STORE_NAME);
        Duration d = Duration.ofSeconds(5);
        context.schedule(d,
                PunctuationType.STREAM_TIME,
                timestamp -> {
                    KeyValueIterator<Long, Transaction> iter = store.all();
                    while(iter.hasNext()) {
                        KeyValue<Long, Transaction> entry = iter.next();
                        Record<Long, Transaction> record = new Record<>(entry.key, entry.value, timestamp);
                        logger.info("Forwarding: "+record);
                        context.forward(record, "Sink");
                        store.delete(entry.key);
                        System.out.println(store.approximateNumEntries());
                    }
                });

    }

    @Override
    public void close() {

    }

    @Override
    public void process(Record<Long, Transaction> record) {
        logger.debug("Processing: "+record);
        store.put(record.key(), record.value());
    }
}
