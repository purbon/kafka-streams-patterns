package com.purbon.kafka.streams.test.utils.model;

public class Transaction {


    private long transaction_id;
    private long card_id;
    private CharSequence user_id;
    private long purchase_id;
    private int store_id;

    public Transaction(long transaction_id, long card_id, CharSequence user_id, long purchase_id, int store_id) {
        this.transaction_id = transaction_id;
        this.card_id = card_id;
        this.user_id = user_id;
        this.purchase_id = purchase_id;
        this.store_id = store_id;
    }

    public long getTransaction_id() {
        return transaction_id;
    }

    public void setTransaction_id(long transaction_id) {
        this.transaction_id = transaction_id;
    }

    public long getCard_id() {
        return card_id;
    }

    public void setCard_id(long card_id) {
        this.card_id = card_id;
    }

    public CharSequence getUser_id() {
        return user_id;
    }

    public void setUser_id(CharSequence user_id) {
        this.user_id = user_id;
    }

    public long getPurchase_id() {
        return purchase_id;
    }

    public void setPurchase_id(long purchase_id) {
        this.purchase_id = purchase_id;
    }

    public int getStore_id() {
        return store_id;
    }

    public void setStore_id(int store_id) {
        this.store_id = store_id;
    }
}
