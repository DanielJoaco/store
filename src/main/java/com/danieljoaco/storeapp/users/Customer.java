package com.danieljoaco.storeapp.users;

import org.mindrot.jbcrypt.BCrypt;

import java.time.LocalDate;

public class Customer extends Users {
    
    private double balance;

    // Constructor con parámetros opcionales: nombre y balance
    
    public Customer(String id, String email, String password, String name) {
        super(id, email, password, UserType.CUSTOMER.name(), name);
        this.balance = 0.0; 
    }
    public Customer(String id, String email, String password, String name, LocalDate createdAt) {
        super(id, email, password, UserType.CUSTOMER.name(), name, createdAt);
        this.balance = 0.0;
    }

    public static Customer loginCustomer(String emailAccess, String passwordAccess){
        Users user = UserDao.findUserByEmail(emailAccess);
        assert user != null;
        if (!BCrypt.checkpw(passwordAccess, user.getPasswordHash()) ||
                !user.getTypeUser().equals(UserType.CUSTOMER.name())) {
            throw new IllegalStateException("Incorrect credentials.");
        }
        System.out.println("Credentials are correct. Welcome " + user.getName());
        return (Customer) user;

    }

    public double getBalance(){
        return this.balance;
    }

    public void setBalance(double balance){
        if (balance < 0) {
            throw new IllegalArgumentException("Balance cannot be negative");
        } else
            this.balance = balance;
    }
    @Override
    public boolean isCustomer() {
        return true;
    }

    @Override
    public String toString() {
        return String.format("%s and her balance is: %.2f", super.toString(), this.balance);
    }
}

