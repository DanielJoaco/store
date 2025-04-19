package com.danieljoaco.storeapp.products;

import com.danieljoaco.storeapp.users.Admin;

public class ProductReference {
    private final String ref;
    private String name;
    private String category;
    private String subcategory;

    public ProductReference(String ref, String name, String category, String subcategory) {
        this.ref = ref;
        this.name = name;
        this.category = category;
        this.subcategory = subcategory;
    }

    // Getters
    public String getRef() { return ref; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public String getSubcategory() { return subcategory; }

    //Setters
    public void setName(String name, Admin admin){
        if(admin.isAdmin()){
            this.name = name;
        }else {
            throw new IllegalArgumentException("Only an admin can edit the name");
        }
    }
    public void setCategory(String category, Admin admin){
        if(admin.isAdmin()){
            this.category = category;
        }else {
            throw new IllegalArgumentException("Only an admin can edit the name");
        }
    }
    public void setSubcategory(String subcategory, Admin admin){
        if(admin.isAdmin()){
            this.subcategory = subcategory;
        }else {
            throw new IllegalArgumentException("Only an admin can edit the name");
        }
    }
}
