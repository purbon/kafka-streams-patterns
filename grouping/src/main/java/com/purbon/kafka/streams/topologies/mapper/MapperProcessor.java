package com.purbon.kafka.streams.topologies.mapper;

import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;

public class MapperProcessor  implements Processor<String, String, String, String> {

    ProcessorContext<String, String> context;
    KeyValueStore<String, String> mapperStore;

    @Override
    public void init(ProcessorContext<String, String> context) {
        this.context = context;
        this.mapperStore = context.getStateStore("mapper");
    }

    @Override
    public void process(Record<String, String> record) {

        var yourJson = record.value();
        if (mapperStore.get("yourAttribute") != null) {
            // do the transforamtion
        }
        var newRecord = new Record<String, String>(record.key(), "NewValue", record.timestamp(), record.headers());

        context.forward(newRecord, "Sink");

    }

    @Override
    public void close() {

    }
}
