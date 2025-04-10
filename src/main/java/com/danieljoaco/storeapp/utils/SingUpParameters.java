package com.danieljoaco.storeapp.utils;

public class SingUpParameters {

    private final String name;
    private final String email;
    private final String id;
    private final String password;

    public SingUpParameters(String name, String email, String id, String password) {
        this.name = name;
        this.email = email;
        this.id = id;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getId() {
        return id;
    }

    public String getPassword() {
        return password;
    }

}
