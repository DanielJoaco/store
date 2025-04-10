package com.danieljoaco.storeapp.utils;

public class LoginInParameters {

    private final String email;
    private final String password;

    public LoginInParameters(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
