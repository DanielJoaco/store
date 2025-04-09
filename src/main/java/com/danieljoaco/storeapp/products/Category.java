
package com.danieljoaco.storeapp.products;

public abstract class   Category {
    
    private final String name;
    
    public enum Categories {
        LIPS, EYES, SKIN, ACCESSORIES, SKINCARE
    }

    public Category(String name) {
        if (Categories.valueOf(name.toUpperCase()).name().equals(name.toUpperCase())
        ) {
            throw new IllegalArgumentException("Invalid category : " + name);
        }
        this.name = name.toUpperCase();
    }

    public String getCategoryName(){
        return this.name;
    }

}
