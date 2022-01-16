package com.purbon.kafka.streams;

import com.purbon.kafka.streams.topologies.OltpTopicNameExtractor;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TopicNameExtractorTest {

    @Test
    public void testReplacement() {
        String regexp = "cdc.(.*).sqlserver.qa2_tlv_oltp2\\.(.*)\\.(.*)\\.(.*)";
        String replacement = "tlv.$1.$2.$3.$4";

        String topicName = "cdc.qa2.sqlserver.qa2_tlv_oltp2.online.dbo.CCC";
        OltpTopicNameExtractor extractor = new OltpTopicNameExtractor(regexp, replacement);
        String newTopicName = extractor.route(topicName);
        assertThat(newTopicName).isEqualTo("tlv.qa2.online.dbo.CCC");
    }
}
