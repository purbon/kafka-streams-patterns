package com.purbon.kafka.streams.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
public class Passenger {

    private String passengerId;
    private String name;
    private String sitCode;
    private String flightId;
    private Timestamp flightDate;

}
