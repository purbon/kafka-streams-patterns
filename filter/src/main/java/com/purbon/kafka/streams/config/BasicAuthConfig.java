package com.purbon.kafka.streams.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.view.script.ScriptTemplateViewResolver;

@Component
@ConfigurationProperties("spring.kafka.properties.basic.auth")
@Data
@NoArgsConstructor
public class BasicAuthConfig {

    private String credentials_source;
    private String user_info;

    public boolean isEmpty() {
        if (credentials_source == null && user_info == null) {
            return true;
        }
        return credentials_source.isBlank() && user_info.isBlank();
    }
}
