package com.danieljoaco.storeapp.products;
import com.danieljoaco.storeapp.db.ProductsDao;
import com.danieljoaco.storeapp.users.*;
import com.danieljoaco.storeapp.db.ProductsDao.*;
import java.util.UUID;

public class Products {
    
    private final String id;
    private String name;
    private SubCategory subCategory;
    private double price;
    private int quantity;
    private Rating rating;
    
    public Products(String name, double price, int quantity, String category, String subcategory){
            this.id = UUID.randomUUID().toString();
            this.name = name;
            this.price = price;
            this.quantity = quantity;
            this.subCategory = new SubCategory(category, subcategory);
    }

    public void setName(String name, Admin admin){
        if (admin.isAdmin()){
            this.name = name;
        } else {
            throw new IllegalArgumentException("Only admins can change the name of a product.");
        }
    }

    public void setPrice(double price, Admin admin){
        if (admin.isAdmin()){
            this.price = price;
        } else {
            throw new IllegalArgumentException("Only admins can change the price of a product.");
        }
    }

    public void setQuantity(int quantity, Admin admin){
        if (admin.isAdmin()){
            this.quantity = quantity;
        } else {
            throw new IllegalArgumentException("Only admins can change the quantity of a product.");
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
    public double getPrice(){
        return this.price;
    }
    public int getQuantity(){
        return this.quantity;
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
