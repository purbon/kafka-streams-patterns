package com.purbon.streaming.RuleEngine.topology;

import com.purbon.streaming.RuleEngine.service.RulesService;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Named;
import org.apache.kafka.streams.kstream.Produced;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RuleEngineTopology {

    Logger logger = LoggerFactory.getLogger(RuleEngineTopology.class);

    private String sourceTopic;

    private String targetTopic;

    private final RulesService rulesService;

    public RuleEngineTopology(@Value("${app.source.topics}") String sourceTopic,
                              @Value("${app.target.topics}") String targetTopic) {
        this.sourceTopic = sourceTopic;
        this.targetTopic = targetTopic;
        this.rulesService = new RulesService();
    }

    @Bean
    public KStream<String, String> process(StreamsBuilder streamsBuilder) {

        KStream<String, String> myStream = streamsBuilder
                .stream(sourceTopic, Consumed.with(Serdes.String(), Serdes.String()));

                myStream
                        .transform(() -> new RuleMatcherTransformer(rulesService), Named.as("ruleMatcher"))
                        .to(targetTopic, Produced.with(Serdes.String(), Serdes.String()));

        return myStream;

    }
}
