package com.purbon.kafka.streams.topologies;

import com.purbon.kafka.streams.model.TransactionE;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;

public class DropAllProcessor implements Processor<Long, TransactionE, Void, Void> {

    @Override
    public void init(ProcessorContext<Void, Void> context) {
    }

    @Override
    public void close() {

    }

    @Override
    public void process(Record record) {

    }
}
