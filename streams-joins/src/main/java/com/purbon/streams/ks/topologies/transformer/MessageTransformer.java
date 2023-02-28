package com.purbon.streams.ks.topologies.transformer;

import com.purbon.streams.ks.model.Quote;
import com.purbon.streams.ks.services.WebApiCallService;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Transformer;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

public class MessageTransformer implements Transformer<String, String, KeyValue<String, String>> {

    Logger logger = LoggerFactory.getLogger(MessageTransformer.class);

    private WebApiCallService apiService;

    public MessageTransformer(WebApiCallService apiService) {
        this.apiService = apiService;
    }

    @Override
    public void init(ProcessorContext context) {

    }

    @Override
    public KeyValue<String, String> transform(String key, String value) {
        Mono<Quote> monoQuote = apiService.quoteRestCall();
        Quote quote = monoQuote.block();
        KeyValue<String, String> nkv = KeyValue.pair(key, value+ " - " + quote.getQuote());
        logger.debug("MT: " + nkv);
        return nkv;
    }

    @Override
    public void close() {

    }
}
