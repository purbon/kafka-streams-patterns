package com.purbon.kafka.streams.config.schema;

import lombok.Data;

@Data
public class Ssl {
    private Keystore keystore;
    private Keystore truststore;
    private Key key;
}
