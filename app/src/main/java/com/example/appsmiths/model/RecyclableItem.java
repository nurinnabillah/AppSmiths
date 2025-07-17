package com.example.appsmiths.model;

public class RecyclableItem {

    private int item_id;

    private String item_name;

    private double price_per_kg;

    public RecyclableItem() {
    }

    public RecyclableItem(int item_id, String item_name, double price_per_kg) {
        this.item_id = item_id;
        this.item_name = item_name;
        this.price_per_kg = price_per_kg;
    }

    public int getItem_id() {
        return item_id;
    }

    public void setItem_id(int item_id) {
        this.item_id = item_id;
    }

    public String getItem_name() {
        return item_name;
    }

    public void setItem_name(String item_name) {
        this.item_name = item_name;
    }

    public double getPrice_per_kg() {
        return price_per_kg;
    }

    public void setPrice_per_kg(double price_per_kg) {
        this.price_per_kg = price_per_kg;
    }

    public String getItemName() {
        return item_name;
    }

    public int getId() {
        return item_id;
    }

    @Override
    public String toString() {
        return "RecyclableItem{" +
                "item_id=" + item_id +
                ", item_name='" + item_name + '\'' +
                ", price_per_kg=" + price_per_kg +
                '}';
    }
}
