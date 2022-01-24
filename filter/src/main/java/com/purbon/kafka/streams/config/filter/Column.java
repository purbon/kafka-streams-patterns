package com.purbon.kafka.streams.config.filter;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class Column {
    private String min;
    private String max;
}
