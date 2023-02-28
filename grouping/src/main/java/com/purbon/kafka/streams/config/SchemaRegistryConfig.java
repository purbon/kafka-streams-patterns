package com.purbon.kafka.streams.config;

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
    private String ssl_keystore_password;
    private String ssl_keystore_location;
    private String ssl_truststore_password;
    private String ssl_truststore_location;
    private String ssl_key_password;

    public Map<String, String> asMap() {
        Map<String, String> config = new HashMap<>();
        config.put("schema.registry.url", url);
        if (url.startsWith("https://")) {
            config.put("schema.registry.ssl.keystore.password", ssl_keystore_password);
            config.put("schema.registry.ssl.keystore.location", ssl_keystore_location);
            config.put("schema.registry.ssl.truststore.password", ssl_truststore_password);
            config.put("schema.registry.ssl.truststore.location", ssl_truststore_location);
            config.put("schema.registry.ssl.key.password", ssl_key_password);
        }
        return config;
    }
}
