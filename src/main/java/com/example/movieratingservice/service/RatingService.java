package com.example.movieratingservice.service;

import com.example.movieratingservice.model.Rating;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RatingService {

    private final RedisTemplate<String, String> redisTemplate;

    @Autowired
    public RatingService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void addRating(Rating rating) {
        String key = "movie:" + rating.movieId();
        String field = rating.customerId();
        String value = rating.rating() + ":" + rating.date();
        
        redisTemplate.opsForHash().put(key, field, value);
        
        // Add to customer's movie list
        String customerKey = "customer:" + rating.customerId();
        redisTemplate.opsForSet().add(customerKey, rating.movieId());
    }
}

