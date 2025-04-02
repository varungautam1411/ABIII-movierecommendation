package com.example.movieratingservice.controller;

import com.example.movieratingservice.model.Rating;
import com.example.movieratingservice.service.RatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ratings")
public class RatingController {

    private final RatingService ratingService;

    @Autowired
    public RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    @PostMapping
    public ResponseEntity<String> addRating(@RequestBody Rating rating) {
        ratingService.addRating(rating);
        return ResponseEntity.ok("Rating added successfully");
    }
}

    
