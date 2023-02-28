package com.purbon.streams.ks.model.envelopes;

public class MsgEnvelop implements Message {

    private String payload;
    private MessageStatus status;
    private Integer retries;

    public MsgEnvelop(String payload) {
        this(payload, 0, MessageStatus.UNDEFINED);
    }

    public MsgEnvelop(String payload, MessageStatus status) {
        this(payload, 0,  status);
    }

    public MsgEnvelop(String payload, Integer retries, MessageStatus status) {
        this.payload = payload;
        this.status = status;
        this.retries = retries;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
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
