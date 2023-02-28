package com.purbon.kafka.streams.serdes;

import java.io.IOException;
import java.util.Arrays;

import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonDeserializer implements Deserializer<JsonNode> {

    protected final ObjectMapper mapper;

    public JsonDeserializer() {
        this.mapper = new ObjectMapper();
    }

    @Override
    public JsonNode deserialize(String topic, byte[] data) {

        try {
            return mapper.readTree(data);
        } catch (IOException e) {
            e.printStackTrace();
            throw new SerializationException(
                    "Can't deserialize data '" + Arrays.toString(data) + "' from topic '" + topic + "'", e);
        }

    }

}

