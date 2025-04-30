package com.danieljoaco.storeapp.product;
import com.danieljoaco.storeapp.user.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class Product {
    
    private final String id;
    private String name;
    private String brand;
    private final String ref;
    private double cost;
    private double price;
    private int stock;
    private String bill;
    private final LocalDate date;
    private final String formattedDate;
    private SubCategory subCategory;
    private String description;
    private Rating rating;
    
    public Product(String name, String brand, String ref, double cost, double price, int stock, String bill, String category, String subcategory, String description) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.brand = brand;
        this.ref = ref;
        this.cost = cost;
        this.price = price;
        this.stock = stock;
        this.bill = bill;
        this.date = LocalDate.now();
        this.formattedDate = DateTimeFormatter.ofPattern("dd/MM/yy").format(date);
        this.subCategory = new SubCategory(category, subcategory);
        this.description = description;
    }

    public Product(String id, String name, String brand, String ref, double cost, double price, int stock, String bill, LocalDate date, String category, String subcategory, String description) {
        this.id = id;
        this.name = name;
        this.brand = brand;
        this.ref = ref;
        this.cost = cost;
        this.price = price;
        this.stock = stock;
        this.bill = bill;
        this.date = date;
        this.formattedDate = DateTimeFormatter.ofPattern("dd/MM/yy").format(date);
        this.subCategory = new SubCategory(category, subcategory);
        this.description = description;
    }
    public void setName(String name, Admin admin){
        if (admin.isAdmin()){
            this.name = name;
        } else {
            throw new IllegalArgumentException("Only admins can change the name of a product.");
        }
    }
    public void setBrand(String brand, Admin admin){
        if (admin.isAdmin()){
            this.brand = brand;
        } else {
            throw new IllegalArgumentException("Only admins can change the brand of a product.");
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
     public void addRating(int rating, String comment, Customer customer){
        if (customer.isCustomer()){
            this.rating = new Rating(rating, comment, customer.getName());
        }
     }
    public void setDescription(String description, Admin admin){
            if (admin.isAdmin()){
                this.description = description;
            } else {
                throw new IllegalArgumentException("Only admins can change the description of a product.");
            }
        }

    public String getId(){return this.id;}
    public String getName(){return this.name;}
    public String getBrand(){return this.brand;}
    public String getRef(){return this.ref;}
    public double getCost(){return this.cost;}
    public double getPrice(){return this.price;}
    public int getStock(){return this.stock;}
    public String getBill(){return this.bill;}
    public LocalDate getDate(){return this.date;}
    public String getFormattedDate(){return this.formattedDate;}
    public String getSubCategory(){return this.subCategory.getName();}
    public String getCategory(){return this.subCategory.getCategoryName();}
    public String getDescription(){return this.description;}
    public Rating getRating(){return this.rating;}

    @Override
    public String toString(){
        return String.format("The product name is: %s, her brand is: %s and the reference is: %s /nDescription: %s", this.name, this.brand, this.ref, this.description);
    }
}
