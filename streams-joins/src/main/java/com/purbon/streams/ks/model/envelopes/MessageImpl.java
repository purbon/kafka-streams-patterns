package com.purbon.streams.ks.model.envelopes;

public class MessageImpl<T> implements Message {

    private T payload;
    private MessageStatus status;
    private Integer retries;

    public MessageImpl(T payload) {
        this(payload, 0, MessageStatus.UNDEFINED);
    }

    public MessageImpl(T payload, MessageStatus status) {
        this(payload, 0,  status);
    }

    public MessageImpl(T payload, Integer retries, MessageStatus status) {
        this.payload = payload;
        this.status = status;
        this.retries = retries;
    }

    public T getPayload() {
        return payload;
    }

    public void setPayload(T payload) {
        this.payload = payload;
    }


    public void setStatus(MessageStatus status) {
        this.status = status;
    }

    public MessageStatus getStatus() {
        return status;
    }

    public Integer getRetries() {
        return retries;
    }

    public void setRetries(Integer retries) {
        this.retries = retries;
    }

    @Override
    public String toString() {
        return payload.toString();
    }
}
