package com.backmore.secondhand_mall.service;

import com.backmore.secondhand_mall.entity.Review;
import java.util.List;

public interface ReviewService {
    Review createReview(Review review);
    Review getReviewById(Long id);
    List<Review> getReviewsByProductId(Long productId);
    List<Review> getReviewsByUserId(Long userId);
    List<Review> getReviewsByProductIdAndRating(Long productId, Integer rating);
    Double getAverageRatingByProductId(Long productId);
    List<Review> getReviewsByStatus(String status);
    Review updateReview(Review review);
    void deleteReview(Long id);
    void reviewModeration(Long id, String status);
}