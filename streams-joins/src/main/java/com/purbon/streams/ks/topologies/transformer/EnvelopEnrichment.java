package com.purbon.streams.ks.topologies.transformer;


import com.purbon.streams.ks.model.envelopes.MsgEnvelop;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.streams.kstream.ValueTransformer;
import org.apache.kafka.streams.processor.ProcessorContext;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class EnvelopEnrichment implements ValueTransformer<MsgEnvelop, MsgEnvelop> {

    private static final String RETRIES = "retries";
    private static Charset encodingCharset = StandardCharsets.UTF_8;

    private ProcessorContext context;

    @Override
    public void init(ProcessorContext context) {
        this.context = context;
    }

    @Override
    public MsgEnvelop transform(MsgEnvelop value) {
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
