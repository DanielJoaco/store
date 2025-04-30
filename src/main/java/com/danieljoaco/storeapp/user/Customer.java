package com.danieljoaco.storeapp.user;

import org.mindrot.jbcrypt.BCrypt;

import java.time.LocalDate;

public class Customer extends User {
    
    private double balance;
    private String phoneNumber;

    public Customer(String id, String email, String password, String name, String phoneNumber) {
        super(id, email, password, UserType.CUSTOMER.name(), name);
        this.phoneNumber = String.valueOf(phoneNumber);
        this.balance = 0.0; 
    }
    public Customer(String id, String email, String password, String name, String phoneNumber, LocalDate createdAt) {
        super(id, email, password, UserType.CUSTOMER.name(), name, createdAt);
        this.phoneNumber = String.valueOf(phoneNumber);
        this.balance = 0.0;
    }

    public static Customer loginCustomer(String emailAccess, String passwordAccess){
        User user = UserDao.findUserByEmail(emailAccess);
        assert user != null;
        if (!BCrypt.checkpw(passwordAccess, user.getPasswordHash()) ||
                !user.getTypeUser().equals(UserType.CUSTOMER.name())) {
            throw new IllegalStateException("Incorrect credentials.");
        }
        System.out.println("Credentials are correct. Welcome " + user.getName());
        return (Customer) user;

    }

    public double getBalance(){return this.balance;}
    public String getPhoneNumber(){return this.phoneNumber;}

    public void setBalance(double balance){
        if (balance < 0) {
            throw new IllegalArgumentException("Balance cannot be negative");
        } else
            this.balance = balance;
    }

    public void setPhoneNumber(int phoneNumber){
        if(phoneNumber < 5 || phoneNumber > 15){
            throw new IllegalArgumentException("Phone number must be between 5 and 15 digits");
        }
        this.phoneNumber = String.valueOf(phoneNumber);
    }

    public CustomerInfo getCustomerInfo(){
        return new CustomerInfo(
                this.getId(),
                this.getName(),
                this.getEmail(),
                this.phoneNumber);
    }


    @Override
    public boolean isCustomer() {
        return true;
    }

    @Override
    public String toString() {
        return String.format("%s and her balance is: %.2f", super.toString(), this.balance);
    }

    public static record CustomerInfo(
            String id,
            String name,
            String email,
            String phoneNumber)
    {}
}

