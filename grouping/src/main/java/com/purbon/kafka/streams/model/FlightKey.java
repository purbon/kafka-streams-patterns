package com.purbon.kafka.streams.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
public class Flight {

    private String id;
    private Timestamp date;
    private String origin;
    private String destination;
}
