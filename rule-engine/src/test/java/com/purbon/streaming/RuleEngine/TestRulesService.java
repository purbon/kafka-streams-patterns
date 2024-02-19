package com.purbon.streaming.RuleEngine;

import com.purbon.streaming.RuleEngine.model.Rule;
import com.purbon.streaming.RuleEngine.service.RulesService;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class TestRulesService {

    @Test
    public void testRulesService() {

        RulesService rs = new RulesService();
        Optional<Rule> doMatch = rs.matchMessage("Esto esto es una casa");
        assertThat(doMatch).contains(rs.getRule(1));

        doMatch = rs.matchMessage("Esto esto va a hacer nada");
        assertThat(doMatch).isEmpty();

        doMatch = rs.matchMessage("Y este para kafka");
        assertThat(doMatch).contains(rs.getRule(2));


    }
}
