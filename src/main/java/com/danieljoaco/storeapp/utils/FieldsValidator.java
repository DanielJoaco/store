package com.danieljoaco.storeapp.utils;
import java.util.regex.Pattern;

public class FieldsValidator {
    
    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[\\w.+-]+@[\\w.-]+$");

    private static final Pattern ID_PATTERN = 
        Pattern.compile("^[0-9]{5,15}$");

    private static final Pattern PASSWORD_PATTERN = 
        Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)[\\w@#&$%-./]{8,30}$");
    
    private static final Pattern ALPHABETIC_INPUT =
            Pattern.compile("^[A-Za-z\\s]{1,30}+$");

    private static final Pattern ALPHANUMERIC_INPUT =
            Pattern.compile("^\\w{2,50}+$");

    private static final Pattern LOCATION =
            Pattern.compile("^[a-zA-ZÀ-ÿ\\s]{2,50}+$");

    private static final Pattern POSTAL_CODE =
            Pattern.compile("^[0-9]{5,10}+$");

    private static final Pattern PHONE_NUMBER =
            Pattern.compile("^(\\+\\d{0,3}[\\s-]?)?(\\d[\\d\\s-]{0,15})$");

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidId(String id) {
        return id != null && ID_PATTERN.matcher(id).matches();
    }

    public static boolean isValidPassword(String password) {
        return password != null && PASSWORD_PATTERN.matcher(password).matches();
    }
    
    public static boolean isValidAlphabeticInput(String name){
        return name != null && ALPHABETIC_INPUT.matcher(name).matches();
    }

    public static boolean isValidAlphanumericInput(String productsInputs){
        return productsInputs != null && ALPHANUMERIC_INPUT.matcher(productsInputs).matches();
    }

    public static boolean isValidLocation(String location){
        return location != null && LOCATION.matcher(location).matches();
    }

    public static boolean isValidPostalCode(String postalCode){
        return postalCode != null && POSTAL_CODE.matcher(postalCode).matches();
    }

    public static boolean isValidPhone(String phoneNumber){
        return phoneNumber != null && PHONE_NUMBER.matcher(phoneNumber).matches();
    }
}
