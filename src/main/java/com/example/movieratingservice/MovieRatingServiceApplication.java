package com.example.movieratingservice;

import com.example.movieratingservice.model.Rating;
import com.example.movieratingservice.service.RatingService;

import java.util.function.Function;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class MovieRatingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MovieRatingServiceApplication.class, args);
    }

    @Bean
    public Function<Rating, String> addRating(RatingService ratingService) {
        return rating -> {
            ratingService.addRating(rating);
            return "Rating added successfully";
        };
    }
}
