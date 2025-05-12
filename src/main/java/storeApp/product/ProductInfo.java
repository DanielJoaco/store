package storeApp.product;

import storeApp.user.Admin;

public class ProductInfo {
    private final String ref;
    private String name;
    private String brand;
    private String category;
    private String subcategory;
    private String description;

    public ProductInfo(String ref, String name, String brand, String category, String subcategory, String description) {
        this.ref = ref;
        this.name = name;
        this.brand = brand;
        this.category = category;
        this.subcategory = subcategory;
        this.description = description;
    }

    // Getters
    public String getRef() { return ref; }
    public String getName() { return name; }
    public String getBrand() { return brand; }
    public String getCategory() { return category; }
    public String getSubcategory() { return subcategory; }
    public String getDescription() { return description; }

    //Setters
    public void setName(String name, Admin admin){
        if(admin.isAdmin()){
            this.name = name;
        }else {
            throw new IllegalArgumentException("Only an admin can edit the name");
        }
    }
    public void setBrand(String brand, Admin admin){
        if(admin.isAdmin()){
            this.brand = brand;
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
    public void setDescription(String description, Admin admin){
        if(admin.isAdmin()){
            this.description = description;
        }else {
            throw new IllegalArgumentException("Only an admin can edit the name");
        }
    }
}
