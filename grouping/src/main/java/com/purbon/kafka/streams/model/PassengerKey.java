package com.purbon.kafka.streams.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
public class FlightKey {

    private String id;
    private Timestamp date;
}
