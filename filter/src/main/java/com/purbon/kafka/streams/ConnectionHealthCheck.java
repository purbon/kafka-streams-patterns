package com.purbon.kafka.streams;

import org.apache.kafka.clients.admin.AdminClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class ConnectionHealthCheck {

    @Qualifier("healthAdminClient")
    @Autowired
    AdminClient admin;

    private boolean isConnectionPossible;

    public ConnectionHealthCheck() {
        this.isConnectionPossible = true;
    }

    @Scheduled(fixedDelay = 20000)
    public void verifyConnection() {
        isConnectionPossible = verifyConnectionToTheBrokers();
    }

    public boolean isConnectionPossible() {
        return isConnectionPossible;
    }


    private boolean verifyConnectionToTheBrokers() {
        try {
            var result = admin.listTopics();
            result.names().get(1, TimeUnit.SECONDS);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }


}
