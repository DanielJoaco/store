package com.danieljoaco.storeapp.users;

import static com.danieljoaco.storeapp.utils.UserValidator.*;
import org.mindrot.jbcrypt.BCrypt;

public abstract class Users {
    
    private String id;
    private String name;
    private String email;
    private String passwordHash;
    private final String typeUser;  

   
    public Users(String id, String email, String password, String typeUser, String name) {
        
        validateFields(id, email, password, name);

        if (isValidTypeUser(typeUser)) {
            this.typeUser = typeUser; 
        } else {
            throw new IllegalArgumentException("Invalid type of user.");
        }
    }
    public Users(String id, String email, String password, String typeUser, String name, boolean isIstance) {

        if(isIstance){
            this.id = id;
            this.email = email;
            this.passwordHash =  password;
            this.typeUser = typeUser;
            this.name = name;
        } else {
            throw new IllegalArgumentException("Only use this constructor for instances.");
        }
    }
    
    private void validateFields(String id, String email, String password, String name) {
        
        if (isValidId(id)) {
            this.id = id;
        } else {
            throw new IllegalArgumentException("The id must only contain numbers and a length between 5 and 15");  
        }
        
        if (isValidEmail(email)) {
            this.email = email;
        } else {
            throw new IllegalArgumentException("The email format must be example@example");
        }
        
        if (isValidPassword(password)) {
            String salt = BCrypt.gensalt(12);
            String hashedPassword = BCrypt.hashpw(password, salt);
            this.passwordHash = hashedPassword;
        } else {
            throw new IllegalArgumentException("The password must be between 8 and 20 characters, at least one letter and one number, and can contain alphanumeric characters and the symbols &%$#_.-");
        }
        if (isValidName(name)) {
            this.name = name;
        } else {
            throw new IllegalArgumentException("The name can only contain alphabetic characters.");
        }
    }

    private boolean isValidTypeUser(String typeUser) {
        try {
            UserType.valueOf(typeUser.toUpperCase());  
            return true;
        } catch (IllegalArgumentException e) {
            return false;  
        }
    }

    public enum UserType {
        ADMIN, CUSTOMER, SUPPORT_AGENT
    }
   
    public String getId(){
        return this.id;
    }
    public String getName(){
        return this.name;
    }
    public String getEmail(){
        return this.email;
    }
    public String getTypeUser(){
        return this.typeUser;
    }
    public String getPasswordHash(){
        return this.passwordHash;
    }
    
    private boolean isPasswordValid(String password) {
        return this.passwordHash != null && this.passwordHash.equals(password);
    }
    
    public void setId(String password, String id){
        if (isPasswordValid(password)){
            this.id = id;
        } else {
            throw new IllegalArgumentException("Incorrect password.");
        }
    }

    public void setName(String name){
        this.name = name;
    }

    public void setEmail(String password, String email){
        if (isPasswordValid(password)){
            this.email = email;
        } else {
            throw new IllegalArgumentException("Incorrect password.");
        }
    }

    public void setPassword(String password, String newPassword){
        if (isPasswordValid(password)){
            this.passwordHash = newPassword;
        } else {
            throw new IllegalArgumentException("Incorrect password.");
        }
    }
    
    public boolean isAdmin() {
        return false;
    }
    public boolean isCustomer() {
        return false;
    }
    
    @Override
    public String toString() {
        return String.format(
            "The user name is: %s and her email is: %s \nType of user is: %s ",
            this.name != null ? this.name : "Unknown", 
            this.email != null ? this.email : "Unknown",
            this.typeUser != null ? this.typeUser : "Unknown"
        );
    }
}
