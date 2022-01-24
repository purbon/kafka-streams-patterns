package com.purbon.kafka.streams.config.schema;

import lombok.Data;

@Data
public class Keystore {
    private String password;
    private String location;
}
