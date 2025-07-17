package com.example.appsmiths.model;

public class UpdatedStatus {
    private String status;
    private double weight;
    private double total_price;

    // Constructor with all fields
    public UpdatedStatus(String status, double weight, double total_price) {
        this.status = status;
        this.weight = weight;
        this.total_price = total_price;
    }

    // Getters and setters
    public String getStatus() {
        return status;
    }

    public double getWeight() {
        return weight;
    }

    public double getTotal_price() {
        return total_price;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public void setTotal_price(double total_price) {
        this.total_price = total_price;
    }
}
