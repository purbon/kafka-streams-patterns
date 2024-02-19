package com.purbon.streaming.RuleEngine.model;

import lombok.Getter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Rule {

    @Getter
    private final long ruleId;
    private final Pattern pattern;

    @Getter
    private String regexp;

    public Rule(long ruleId, String regexp) {
        this.ruleId = ruleId;
        this.regexp = regexp;
        this.pattern = Pattern.compile(regexp);
    }

    public boolean match(String value) {
        Matcher m = pattern.matcher(value);
        return m.matches();
    }
}
