package com.danieljoaco.storeapp.utils;
import java.util.regex.Pattern;

public class UserValidator {
    
    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[\\w.+-]+@[\\w.-]+$");

    private static final Pattern ID_PATTERN = 
        Pattern.compile("^[0-9]{5,15}$");

    private static final Pattern PASSWORD_PATTERN = 
        Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)[\\w@#&$%-./]{8,60}$");
    
    private static final Pattern NAME =
            Pattern.compile("^[A-Za-z\\s]+$");

    private static final Pattern PRODUCTS_INPUTS =
            Pattern.compile("^\\w+$");

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidId(String id) {
        return id != null && ID_PATTERN.matcher(id).matches();
    }

    public static boolean isValidPassword(String password) {
        return password != null && PASSWORD_PATTERN.matcher(password).matches();
    }
    
    public static boolean isValidName(String name){
        return name != null && NAME.matcher(name).matches();
    }

    public static boolean isValidProductsInputs(String productsInputs){
        return productsInputs != null && PRODUCTS_INPUTS.matcher(productsInputs).matches();
    }
}
