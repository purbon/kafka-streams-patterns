package com.purbon.streaming.RuleEngine.service;

import com.purbon.streaming.RuleEngine.model.Rule;
import org.apache.kafka.common.quota.ClientQuotaAlteration;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RulesService {

    private List<Rule> rules;

    public RulesService() {
        this.rules = buildRules();
    }

    public Optional<Rule> matchMessage(String message) {
        Optional<Rule> doMatch = Optional.empty();
        for(Rule rule : rules) {
            if (rule.match(message)) {
                doMatch = Optional.of(rule);
                break;
            }
        }
        return doMatch;
    }

    private List<Rule> buildRules() {
        List<Rule> rules = new ArrayList<>();
        rules.add(new Rule(1, "^.*casa.*$"));
        rules.add(new Rule(2, "^.*kafka.*$"));
        return rules;
    }

    public Rule getRule(int ruleId) {
        return rules.get(ruleId-1);
    }
}
