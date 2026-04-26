package com.elite.township;

import java.util.Date;
import java.util.UUID;

public class Order {
    private int id;
    private String description;
    private double price;
    private Date createdAt;
    private String transactionId;

    public Order() {
    }

    public Order(int id, String description, double price) {
        this.id = id;
        this.description = description;
        this.price = price;
        this.createdAt = new Date();
        this.transactionId = UUID.randomUUID().toString();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

        public void setCreatedAt(Date createdAt) {
            this.createdAt = createdAt;
        }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
}
