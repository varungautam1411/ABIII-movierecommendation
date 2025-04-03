package com.example.movieratingservice.model;

import java.io.Serializable;

public class Rating implements Serializable {
    private String customerId;
    private String movieId;
    private int rating;
    private String date;

    // Default constructor
    public Rating() {}

    // Constructor with all fields
    public Rating(String customerId, String movieId, int rating, String date) {
        this.customerId = customerId;
        this.movieId = movieId;
        this.rating = rating;
        this.date = date;
    }

    // Getters and Setters
    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getMovieId() {
        return movieId;
    }

    public void setMovieId(String movieId) {
        this.movieId = movieId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "Rating{" +
                "customerId='" + customerId + '\'' +
                ", movieId='" + movieId + '\'' +
                ", rating=" + rating +
                ", date='" + date + '\'' +
                '}';
    }
}
