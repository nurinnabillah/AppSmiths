package com.example.appsmiths.model;

import com.google.gson.annotations.SerializedName;

public class UserRequest {

    private int request_id;

    private int user_id;

    @SerializedName("user")
    private User user;

    private int item_id;

    private String address;

    @SerializedName("request_date")

    private String request_date;

    private String status;

    private String weight;

    private String total_price;

    private String notes;

    @SerializedName("item")
    private RecyclableItem item;

    public UserRequest(int request_id, int user_id, int item_id, String address, String request_date, String status, String weight,
                       String total_price, String notes) {
        this.request_id = request_id;
        this.user_id = user_id;
        this.item_id = item_id;
        this.address = address;
        this.request_date = request_date;
        this.status = status;
        this.weight = weight;
        this.total_price = total_price;
        this.notes = notes;
    }

    public int getRequest_id() {
        return request_id;
    }

    public void setRequest_id(int request_id) {
        this.request_id = request_id;
    }

    public User getUser() {
        return user;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public int getItem_id() {
        return item_id;
    }

    public void setItem_id(int item_id) {
        this.item_id = item_id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getRequest_date() {
        return request_date;
    }

    public void setRequest_date(String request_date) {
        this.request_date = request_date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getTotal_price() {
        return total_price;
    }

    public void setTotal_price(String total_price) {
        this.total_price = total_price;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public RecyclableItem getItem() { return item; }

    @Override
    public String toString() {
        return "UserRequest{" +
                "request_id=" + request_id +
                ", user_id=" + user_id +
                ", item_id=" + item_id +
                ", address='" + address + '\'' +
                ", request_date='" + request_date + '\'' +
                ", status='" + status + '\'' +
                ", weight=" + weight +
                ", total_price=" + total_price +
                ", notes='" + notes + '\'' +
                '}';
    }
}
