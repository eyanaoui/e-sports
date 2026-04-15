package com.esports.models;

public class Product {

    private int id;
    private String category;
    private String description;
    private String image;
    private boolean isActive;
    private String name;
    private double price;
    private int stock;
    private int ordersCount;

    public Product() {
    }

    public Product(int id, String category, String description, String image, boolean isActive, String name, double price, int stock) {
        this.id = id;
        this.category = category;
        this.description = description;
        this.image = image;
        this.isActive = isActive;
        this.name = name;
        this.price = price;
        this.stock = stock;
    }

    public Product(String category, String description, String image, boolean isActive, String name, double price, int stock) {
        this.category = category;
        this.description = description;
        this.image = image;
        this.isActive = isActive;
        this.name = name;
        this.price = price;
        this.stock = stock;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }


    public boolean isActive() {
        return isActive;
    }

    public boolean getActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }


    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public int getOrdersCount() {
        return ordersCount;
    }

    public void setOrdersCount(int ordersCount) {
        this.ordersCount = ordersCount;
    }
}