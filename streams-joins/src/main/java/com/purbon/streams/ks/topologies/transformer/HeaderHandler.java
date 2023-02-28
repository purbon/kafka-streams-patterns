package com.purbon.streams.ks.topologies.transformer;


import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Transformer;
import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class HeaderHandler<K, V> implements Transformer<K, V, KeyValue<K, V>> {

    private static final String RETRIES = "retries";
    private static Charset encodingCharset = StandardCharsets.UTF_8;

    private ProcessorContext context;

    @Override
    public void init(ProcessorContext context) {
        this.context = context;
    }

    @Override
    public KeyValue<K, V> transform(K key, V value) {
        Headers headers = context.headers();
        if (headers.lastHeader(RETRIES) == null) {
            headers.add(RETRIES, "0".getBytes(encodingCharset));
        }

        Integer newValueForRetries = valueAsInteger(headers.lastHeader(RETRIES)) + 1;
        headers.add(RETRIES, newValueForRetries.toString().getBytes(encodingCharset));
        return KeyValue.pair(key, value);
    }

    private Integer valueAsInteger(Header header) {
        String headerAsString = new String(header.value(), encodingCharset);
        return Integer.valueOf(headerAsString);
    }

    @Override
    public void close() {

    }
}
