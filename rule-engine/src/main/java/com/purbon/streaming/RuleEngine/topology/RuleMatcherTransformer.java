package com.purbon.streaming.RuleEngine.topology;

import org.apache.kafka.streams.kstream.ValueTransformer;
import org.apache.kafka.streams.processor.ProcessorContext;

public class RuleMatcherTransformer implements ValueTransformer<String, String> {


    @Override
    public void init(ProcessorContext context) {

    }

    @Override
    public String transform(String value) {
        System.out.println("RuleMatcherTransformer: "+value);
        return value;
    }

    @Override
    public void close() {

    }
}
