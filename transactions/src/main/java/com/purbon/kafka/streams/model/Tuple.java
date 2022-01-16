package com.purbon.kafka.streams.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Tuple<K, V> {

    private K left;
    private V right;

}
