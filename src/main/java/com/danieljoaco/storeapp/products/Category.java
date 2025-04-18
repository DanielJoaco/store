package com.danieljoaco.storeapp.products;

import static com.danieljoaco.storeapp.menu.utils.Utils.capitalize;

public abstract class   Category {
    
    private final String name;
    
    public enum Categories {
        LIPS, EYES, SKIN, ACCESSORIES, SKINCARE
    }

    public Category(String name) {
        try {
            Categories.valueOf(name.toUpperCase());
            this.name = name.toUpperCase();
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid category: " + name);
        }
    }

    public String getCategoryName(){
        return this.name;
    }

}
