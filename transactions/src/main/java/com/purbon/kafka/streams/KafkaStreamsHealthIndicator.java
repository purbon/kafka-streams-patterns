package com.purbon.kafka.streams;

import org.apache.kafka.streams.KafkaStreams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.stereotype.Component;

/**
 * KStreams health indicator task for Spring
 */
@Component
public class KafkaStreamsHealthIndicator implements HealthIndicator, KafkaStreams.StateListener {

    private Throwable currentException = null;
    private KafkaStreams.State state;

    @Qualifier("defaultKafkaStreamsBuilder")
    @Autowired
    private StreamsBuilderFactoryBean kafkaStreams;

    @Autowired
    private ConnectionHealthCheck connectionHealthCheck;

    @Override
    public Health health() {

        var connected = connectionHealthCheck.isConnectionPossible();
        Health.Builder health;

        if (isItHealthy(connected)) {
            health = Health.up();

        } else {
            health = Health.down();
        }

        if (state != null) {
            health = health.withDetail("state", state);
        }

        if (currentException != null) {
            health = health.withDetail("exception", currentException);
        }

        return health
                .withDetail("isRunning", kafkaStreams.isRunning())
                .withDetail("isConnected", connected)
                .build();
    }

    public void setCurrentException(Throwable ex) {
        this.currentException = ex;
    }

    private boolean isItHealthy(boolean connected) {
        if (!connected) {
            return false;
        } else {
            return (state != null) && (state.isRunningOrRebalancing()) || state.equals(KafkaStreams.State.CREATED);
        }
    }

    @Override
    public void onChange(KafkaStreams.State newState, KafkaStreams.State oldState) {
        this.state = newState;
    }
}