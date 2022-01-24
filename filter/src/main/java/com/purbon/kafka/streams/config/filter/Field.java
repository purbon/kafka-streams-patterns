package com.purbon.kafka.streams.config.filter;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class Field {
    private String db;
    private String dbschema;
    private String table;
}
