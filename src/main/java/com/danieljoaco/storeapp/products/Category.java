
package com.danieljoaco.storeapp.products;


public abstract class   Category {
    
    private String id;
    private String name;
    
    public enum Categories {
        LABIOS, OJOS, PIEL, ACCESORIOS, SKINCARE
    }

    public Category(String id, String name) {
        if (!isValidCategory(name)) {
            throw new IllegalArgumentException("Categoría inválida: " + name);
        }
        this.id = id;
        this.name = name.toUpperCase();
    }

    private boolean isValidCategory(String name) {
        try {
            Categories.valueOf(name.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    public String getid(){
        return this.id;
    }

    public String getName(){
        return this.name;
    }
    
    public void setId(String id){
        this.id = id;
    }
    
    public void setName(String name){
        this.name = name;
    }
}
