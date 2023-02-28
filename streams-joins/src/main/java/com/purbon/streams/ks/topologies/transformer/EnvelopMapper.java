package com.purbon.streams.ks.topologies.transformer;


import com.purbon.streams.ks.model.envelopes.MessageImpl;
import com.purbon.streams.ks.model.envelopes.MessageStatus;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Transformer;
import org.apache.kafka.streams.kstream.ValueTransformer;
import org.apache.kafka.streams.processor.ProcessorContext;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class EnvelopMapper implements ValueTransformer<MessageImpl<String>, MessageImpl<String>> {

    private static final String RETRIES = "retries";
    private static Charset encodingCharset = StandardCharsets.UTF_8;

    private ProcessorContext context;

    @Override
    public void init(ProcessorContext context) {
        this.context = context;
    }

    @Override
    public MessageImpl<String> transform(MessageImpl<String> value) {
        Headers headers = context.headers();
        if (headers.lastHeader(RETRIES) == null) {
            headers.add(RETRIES, "0".getBytes(encodingCharset));
        }
        Integer retries = valueAsInteger(headers.lastHeader(RETRIES));
        value.setRetries(retries);
        return value;
    }

    private Integer valueAsInteger(Header header) {
        String headerAsString = new String(header.value(), encodingCharset);
        return Integer.valueOf(headerAsString);
    }

    @Override
    public void close() {

    }
}
