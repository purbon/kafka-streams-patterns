package com.purbon.kafka.streams.config;

import com.purbon.kafka.streams.config.auth.Credentials;
import com.purbon.kafka.streams.config.auth.User;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("spring.kafka.properties.basic.auth")
@Data
@NoArgsConstructor
public class BasicAuthConfig {


    private Credentials credentials;
    private User user;

    public boolean isEmpty() {
        if (credentials == null && user == null) {
            return true;
        }
        return credentials.getSource().isBlank() && user.getInfo().isBlank();
    }
}
