package com.purbon.kafka.streams.test.utils.model;

public class Card {

    private long card_id;
    private CharSequence owner;
    private CharSequence code;
    private int token;

    public Card(long card_id, CharSequence owner, CharSequence code, int token) {
        this.card_id = card_id;
        this.owner = owner;
        this.code = code;
        this.token = token;
    }

    public long getCard_id() {
        return card_id;
    }

    public void setCard_id(long card_id) {
        this.card_id = card_id;
    }

    public CharSequence getOwner() {
        return owner;
    }

    public void setOwner(CharSequence owner) {
        this.owner = owner;
    }

    public CharSequence getCode() {
        return code;
    }

    public void setCode(CharSequence code) {
        this.code = code;
    }

    public int getToken() {
        return token;
    }

    public void setToken(int token) {
        this.token = token;
    }
}
