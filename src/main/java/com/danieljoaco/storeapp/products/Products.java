package com.danieljoaco.storeapp.products;


public class Products {
    
    private String id;
    private String name;
    private Category category; 
    private SubCategory subCategory;
    private double price;
    private double quantity;
    private Rating rating;
    
    public Products(String id, String name, double price, double quantity){
        this.id = id;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }
    
}   
