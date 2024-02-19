package com.purbon.streaming.RuleEngine;

import com.purbon.streaming.RuleEngine.topology.RuleEngineTopology;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.in;

@Testcontainers
public class TestRuleEngineApplication {

	@Container
	static final KafkaContainer kafka = new KafkaContainer(
			DockerImageName.parse("confluentinc/cp-kafka:latest")
	);


	@DynamicPropertySource
	static void overrideProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
	}

	@Test
	public void testRuleEngineFlow() {

		String inputTopicName = "inputTopic";
		String outputTopicName = "outputTopic";

		Properties props = new Properties();
		props.setProperty(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
		props.setProperty(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
		props.setProperty(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());

		StreamsBuilder streamsBuilder = new StreamsBuilder();
		RuleEngineTopology ruleEngineTopology = new RuleEngineTopology(inputTopicName, outputTopicName);
		ruleEngineTopology.process(streamsBuilder);

		Topology topology = streamsBuilder.build();

		TopologyTestDriver driver = new TopologyTestDriver(topology, props);

		TestInputTopic<String, String> inputTopic = driver.createInputTopic(inputTopicName,
				Serdes.String().serializer(),
				Serdes.String().serializer());

		TestOutputTopic<String, String> outputTopic = driver.createOutputTopic(outputTopicName,
				Serdes.String().deserializer(),
				Serdes.String().deserializer());

		List<String> messages = Arrays.asList("Foo", "Bar");
		for (String message : messages) {
			inputTopic.pipeInput(message);
		}
		for(int i=0; i < messages.size(); i++) {
			String message = outputTopic.readValue();
			assertThat(message).isIn(messages);
		}
	}

}
