package com.purbon.kafka.streams.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.sql.Timestamp;

@ToString
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Flight {

    private String id;
    private Timestamp date;
    private String origin;
    private String destination;

    private Passengers passengers;
}
