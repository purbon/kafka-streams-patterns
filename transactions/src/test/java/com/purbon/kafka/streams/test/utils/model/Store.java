package com.purbon.kafka.streams.test.utils.model;

public class Store {

    private int store_id;
    private CharSequence name;

    public Store(int store_id, CharSequence name) {
        this.store_id = store_id;
        this.name = name;
    }

    public int getStore_id() {
        return store_id;
    }

    public void setStore_id(int store_id) {
        this.store_id = store_id;
    }

    public CharSequence getName() {
        return name;
    }

    public void setName(CharSequence name) {
        this.name = name;
    }
}
