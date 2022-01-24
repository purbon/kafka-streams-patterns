package com.purbon.kafka.streams.config;

import com.purbon.kafka.streams.config.schema.Ssl;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@ConfigurationProperties("spring.kafka.properties.schema.registry")
@Data
@NoArgsConstructor
public class SchemaRegistryConfig {

    private String url;
    private Ssl ssl;

    public Map<String, String> asMap() {
        Map<String, String> config = new HashMap<>();
        config.put("schema.registry.url", url);
        if (url.startsWith("https://")) {
            config.put("schema.registry.ssl.keystore.password", ssl.getKeystore().getPassword());
            config.put("schema.registry.ssl.keystore.location", ssl.getKeystore().getLocation());
            config.put("schema.registry.ssl.truststore.password", ssl.getTruststore().getPassword());
            config.put("schema.registry.ssl.truststore.location", ssl.getTruststore().getLocation());
            config.put("schema.registry.ssl.key.password", ssl.getKey().getPassword());
        }
        return config;
    }
}
