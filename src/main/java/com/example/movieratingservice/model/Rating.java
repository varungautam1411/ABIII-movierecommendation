 
package com.example.movieratingservice.model;

public record Rating(String customerId, String movieId, int rating, String date) {}
