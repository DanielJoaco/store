package com.danieljoaco.storeapp.products;
import com.danieljoaco.storeapp.users.*;

import java.time.LocalDate;
import java.util.UUID;

public class Products {
    
    private final String id;
    private String name;
    private String ref;
    private double cost;
    private double price;
    private int stock;
    private String bill;
    private LocalDate date;
    private SubCategory subCategory;
    private Rating rating;
    
    public Products(String name, String ref, double cost, double price, int stock, String bill, String category, String subcategory){
            this.id = UUID.randomUUID().toString();
            this.name = name;
            this.ref = ref;
            this.cost = cost;
            this.price = price;
            this.stock = stock;
            this.bill = bill;
            this.date = LocalDate.now();
            this.subCategory = new SubCategory(category, subcategory);
    }

    public Products(String name, String ref, double cost, double price, int stock, String bill, LocalDate date, String category, String subcategory){
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.ref = ref;
        this.cost = cost;
        this.price = price;
        this.stock = stock;
        this.bill = bill;
        this.date = date;
        this.subCategory = new SubCategory(category, subcategory);
    }

    public void setName(String name, Admin admin){
        if (admin.isAdmin()){
            this.name = name;
        } else {
            throw new IllegalArgumentException("Only admins can change the name of a product.");
        }
    }

    public void setRef(String ref, Admin admin){
        if (admin.isAdmin()){
            this.ref = ref;
        } else {
            throw new IllegalArgumentException("Only admins can change the reference of a product.");
        }
    }

    public void setCost(double cost, Admin admin){
        if (admin.isAdmin()){
            this.cost = cost;
        } else {
            throw new IllegalArgumentException("Only admins can change the cost of a product.");
        }
    }

    public void setPrice(double price, Admin admin){
        if (admin.isAdmin()){
            this.price = price;
        } else {
            throw new IllegalArgumentException("Only admins can change the price of a product.");
        }
    }

    public void setStock(int stock, Admin admin){
        if (admin.isAdmin()){
            this.stock = stock;
        } else {
            throw new IllegalArgumentException("Only admins can change the quantity of a product.");
        }
    }

    public void setBill(String bill, Admin admin){
        if (admin.isAdmin()){
            this.bill = bill;
        } else {
            throw new IllegalArgumentException("Only admins can change the bill of a product.");
        }
    }

     public void setSubCategory(String category, String subcategory, Admin admin){
        if (admin.isAdmin()){
            this.subCategory = new SubCategory(category, subcategory);
        } else {
            throw new IllegalArgumentException("Only admins can change the subcategory of a product.");
        }
     }

     public void addRating(int rating, String comment, Customer customer, String date){
        if (customer.isCustomer()){
            this.rating = new Rating(rating, comment, customer.getName());
        }
     }

     public String getId(){
        return this.id;
     }

    public String getName(){
        return this.name;
    }
    public String getRef(){
        return this.ref;
    }
    public double getCost(){
        return this.cost;
    }
    public double getPrice(){
        return this.price;
    }
    public int getStock(){
        return this.stock;
    }
    public String getBill(){
        return this.bill;
    }
    public String getSubCategory(){
        return this.subCategory.getName();
    }
    public String getCategory(){
        return this.subCategory.getCategoryName();
    }
    public Rating getRating(){
        return this.rating;
    }

}   
