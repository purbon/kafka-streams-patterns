package com.purbon.kafka.streams.topologies;

public class Tuple<L, R> {
    L left;
    R right;

    @Override
    public String toString() {
        return "Tuple{" +
                "left=" + left +
                ", right=" + right +
                '}';
    }
}