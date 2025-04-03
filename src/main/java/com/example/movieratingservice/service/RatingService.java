package com.example.movieratingservice.service;

import com.example.movieratingservice.model.Rating;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RatingService {
    private static final Logger logger = LoggerFactory.getLogger(RatingService.class);
    
    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public RatingService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void addRating(Rating rating) {
        try {
            if (rating == null || rating.getCustomerId() == null || rating.getMovieId() == null) {
                throw new IllegalArgumentException("Rating data cannot be null");
            }

            String movieKey = "movie:" + rating.getMovieId();
            String customerKey = "customer:" + rating.getCustomerId();
            String ratingValue = String.format("%d:%s", rating.getRating(), rating.getDate());

            // Use multi to ensure atomic operation
            redisTemplate.execute(new SessionCallback<List<Object>>() {
                @SuppressWarnings("unchecked")
                @Override
                public List<Object> execute(@SuppressWarnings({ "null", "rawtypes" }) RedisOperations operations) throws DataAccessException {
                    operations.multi();
                    
                    operations.opsForHash().put(movieKey, rating.getCustomerId(), ratingValue);
                    operations.opsForHash().put(customerKey, rating.getMovieId(), ratingValue);
                    operations.opsForSet().add(movieKey + ":customers", rating.getCustomerId());
                    operations.opsForSet().add(customerKey + ":movies", rating.getMovieId());
                    
                    return operations.exec();
                }
            });

            logger.debug("Successfully added rating for customer {} and movie {}", 
                rating.getCustomerId(), rating.getMovieId());
        } catch (Exception e) {
            logger.error("Error adding rating: ", e);
            throw new RuntimeException("Failed to add rating", e);
        }
    }




    public Map<String, String> getMovieRatings(String movieId) {
        try {
            String movieKey = "movie:" + movieId;
            Map<Object, Object> rawData = redisTemplate.opsForHash().entries(movieKey);
            
            // Convert Map<Object, Object> to Map<String, String>
            return rawData.entrySet().stream()
                .collect(Collectors.toMap(
                    entry -> entry.getKey().toString(),
                    entry -> entry.getValue().toString()
                ));
        } catch (Exception e) {
            logger.error("Error retrieving movie ratings: ", e);
            return new HashMap<>();
        }
    }

    public Map<String, String> getCustomerRatings(String customerId) {
        try {
            String customerKey = "customer:" + customerId;
            Map<Object, Object> rawData = redisTemplate.opsForHash().entries(customerKey);
            
            // Convert Map<Object, Object> to Map<String, String>
            return rawData.entrySet().stream()
                .collect(Collectors.toMap(
                    entry -> entry.getKey().toString(),
                    entry -> entry.getValue().toString()
                ));
        } catch (Exception e) {
            logger.error("Error retrieving customer ratings: ", e);
            return new HashMap<>();
        }
    }

    public Double getAverageMovieRating(String movieId) {
        try {
            String movieKey = "movie:" + movieId;
            Map<Object, Object> ratings = redisTemplate.opsForHash().entries(movieKey);
            
            if (ratings.isEmpty()) {
                return 0.0;
            }

            double sum = ratings.values().stream()
                .mapToInt(value -> Integer.parseInt(value.toString().split(":")[0]))
                .sum();

            return sum / ratings.size();
        } catch (Exception e) {
            logger.error("Error calculating average rating: ", e);
            return 0.0;
        }
    }

    @SuppressWarnings("null")
    public Set<String> getCustomerWhoRated(String movieId) {
        try {
            String movieKey = "movie:" + movieId + ":customers";
            Set<Object> rawMembers = redisTemplate.opsForSet().members(movieKey);
            
            // Convert Set<Object> to Set<String>
            return rawMembers.stream()
                .map(Object::toString)
                .collect(Collectors.toSet());
        } catch (Exception e) {
            logger.error("Error getting customers who rated: ", e);
            return Set.of();
        }
    }

    @SuppressWarnings("null")
    public Set<String> getMoviesRatedByCustomer(String customerId) {
        try {
            String customerKey = "customer:" + customerId + ":movies";
            Set<Object> rawMembers = redisTemplate.opsForSet().members(customerKey);
            
            // Convert Set<Object> to Set<String>
            return rawMembers.stream()
                .map(Object::toString)
                .collect(Collectors.toSet());
        } catch (Exception e) {
            logger.error("Error getting movies rated by customer: ", e);
            return Set.of();
        }
    }

    public boolean updateRating(Rating rating) {
        try {
            String movieKey = "movie:" + rating.getMovieId();
            String customerKey = "customer:" + rating.getCustomerId();
            String ratingValue = String.format("%d:%s", rating.getRating(), rating.getDate());

            Boolean exists = redisTemplate.opsForHash().hasKey(movieKey, rating.getCustomerId());
            if (exists == null || !exists) {
                return false;
            }

            redisTemplate.opsForHash().put(movieKey, rating.getCustomerId(), ratingValue);
            redisTemplate.opsForHash().put(customerKey, rating.getMovieId(), ratingValue);

            logger.info("Successfully updated rating");
            return true;
        } catch (Exception e) {
            logger.error("Error updating rating: ", e);
            return false;
        }
    }

    public boolean deleteRating(String customerId, String movieId) {
        try {
            String movieKey = "movie:" + movieId;
            String customerKey = "customer:" + customerId;

            redisTemplate.opsForHash().delete(movieKey, customerId);
            redisTemplate.opsForHash().delete(customerKey, movieId);
            redisTemplate.opsForSet().remove(movieKey + ":customers", customerId);
            redisTemplate.opsForSet().remove(customerKey + ":movies", movieId);

            logger.info("Successfully deleted rating");
            return true;
        } catch (Exception e) {
            logger.error("Error deleting rating: ", e);
            return false;
        }
    }

    public Map<String, Object> getRatingStats(String movieId) {
        Map<String, Object> stats = new HashMap<>();
        try {
            String movieKey = "movie:" + movieId;
            Map<Object, Object> ratings = redisTemplate.opsForHash().entries(movieKey);

            if (ratings.isEmpty()) {
                stats.put("totalRatings", 0);
                stats.put("averageRating", 0.0);
                return stats;
            }

            Map<Integer, Integer> distribution = new HashMap<>();
            double sum = 0;
            int count = 0;

            for (Object value : ratings.values()) {
                int rating = Integer.parseInt(value.toString().split(":")[0]);
                sum += rating;
                count++;
                distribution.merge(rating, 1, Integer::sum);
            }

            stats.put("totalRatings", count);
            stats.put("averageRating", sum / count);
            stats.put("distribution", distribution);

            return stats;
        } catch (Exception e) {
            logger.error("Error getting rating stats: ", e);
            return stats;
        }
    }
}
