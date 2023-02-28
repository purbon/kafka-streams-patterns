package com.purbon.streams.ks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class SimpleApplication {

	private static Logger LOG = LoggerFactory.getLogger(SimpleApplication.class);

	public static void main(String[] args) {
		new SpringApplicationBuilder(SimpleApplication.class)
				.web(WebApplicationType.NONE)
				.run(args);
	}

}
