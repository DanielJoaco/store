package com.danieljoaco.storeapp.products;
import java.util.UUID;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Rating {

    private final String id;
    private final int rating;
    private final String comment;
    private final String date;
    private final String customer;

    public Rating(int rating, String comment, String customer){
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Invalid rating : " + rating);
        }
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        this.id = UUID.randomUUID().toString();
        this.rating = rating;
        this.comment = comment;
        this.customer = customer;
        this.date = today.format(formatter);
    }

    public String getId(){
        return this.id;
    }

    public int getRating(){
        return this.rating;
    }

    public String getComment(){
        return this.comment;
    }

    public String getDate(){
        return this.date;
    }

    public String getCustomer() {
        return this.customer;
    }
}
