package com.purbon.streaming.RuleEngine.topology;

import com.purbon.streaming.RuleEngine.model.Rule;
import com.purbon.streaming.RuleEngine.service.RulesService;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Transformer;
import org.apache.kafka.streams.kstream.ValueTransformer;
import org.apache.kafka.streams.processor.ProcessorContext;

import java.util.Optional;

public class RuleMatcherTransformer implements Transformer<String, String, KeyValue<String, String>> {


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
        String newKey = doMatchRule.map(rule -> String.valueOf(rule.getRuleId())).orElse("no-key");
        System.out.println("Before: " + key + " " + value);
        System.out.println("After: " + newKey + " " + value);

        return new KeyValue<>(newKey, value);
    }

    @Override
    public void close() {

    }
}
