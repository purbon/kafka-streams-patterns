package com.purbon.streaming.RuleEngine.topology;

import com.purbon.streaming.RuleEngine.model.Rule;
import com.purbon.streaming.RuleEngine.service.RulesService;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Utils;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Transformer;
import org.apache.kafka.streams.kstream.ValueTransformer;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class RuleMatcherTransformer implements Transformer<String, String, KeyValue<String, String>> {

    Logger logger = LoggerFactory.getLogger(RuleMatcherTransformer.class);

    private final RulesService rulesService;

    public RuleMatcherTransformer(RulesService rulesService) {
        this.rulesService = rulesService;
    }

    @Override
    public void init(ProcessorContext context) {

    }

    @Override
    public KeyValue<String, String> transform(String key, String value) {

        Optional<Rule> doMatchRule = rulesService.matchMessage(value);
        String newKey = doMatchRule.map(rule -> String.valueOf(rule.getRuleId())).orElse("3");
        int targetPartition = debugTargetPartitionFor(newKey);

        logger.info("Before: " + key + " " + value);
        logger.info("After: " + newKey + " " + value + " " + targetPartition);
        return new KeyValue<>(newKey, value);
    }

    @Override
    public void close() {

    }

    public int debugTargetPartitionFor(String newKey) {
        var serializedKey = Serdes.String().serializer().serialize("bar", newKey);
        int numPartitions = 3;
        return Utils.toPositive(Utils.murmur2(serializedKey)) % numPartitions;
    }


}
