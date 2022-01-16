package com.purbon.kafka.streams.topologies;

import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.streams.processor.RecordContext;
import org.apache.kafka.streams.processor.TopicNameExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OltpTopicNameExtractor implements TopicNameExtractor<GenericRecord, GenericRecord> {

    Logger logger = LoggerFactory.getLogger(OltpTopicNameExtractor.class);

    private Pattern pattern;
    private String replacement;

    public OltpTopicNameExtractor() {
        this("\\w+\\.(.*)\\.(.*)", "$2");
    }

    public OltpTopicNameExtractor(String regexp, String replacement) {
        this.pattern = Pattern.compile(regexp);
        this.replacement = replacement;
    }

    @Override
    public String extract(GenericRecord key, GenericRecord value, RecordContext context) {
        return route(context.topic());
    }

    public String route(String topicName) {
        Matcher m = pattern.matcher(topicName);
        String newTopicName;
        if (m.matches()) {
            newTopicName = m.replaceFirst(replacement);
        } else {
            newTopicName = topicName;
        }
        logger.debug("TopicNameExtractor origin="+topicName+", new="+newTopicName+", matches="+m.matches());
        return newTopicName;
    }

    @Override
    public String toString() {
        return "OltpTopicNameExtractor{" +
                "pattern=" + pattern +
                "replacement=" +replacement +
                '}';
    }
}