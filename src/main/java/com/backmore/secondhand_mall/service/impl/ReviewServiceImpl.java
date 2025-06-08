package com.backmore.secondhand_mall.service.impl;

import com.backmore.secondhand_mall.entity.Product;
import com.backmore.secondhand_mall.entity.Review;
import com.backmore.secondhand_mall.entity.User;
import com.backmore.secondhand_mall.repository.ProductRepository;
import com.backmore.secondhand_mall.repository.ReviewRepository;
import com.backmore.secondhand_mall.repository.UserRepository;
import com.backmore.secondhand_mall.service.ReviewService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class ReviewServiceImpl implements ReviewService {
    private static final Logger logger = LoggerFactory.getLogger(ReviewServiceImpl.class);

    @Autowired
    private ReviewRepository reviewRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public Review createReview(Review review) {
        // 验证关联数据
        validateProductAndUser(review.getProductId(), review.getUserId());
        
        // 检查用户是否已经评价过该商品
        if (reviewRepository.existsByUserIdAndProductId(review.getUserId(), review.getProductId())) {
            throw new IllegalStateException("用户已经评价过该商品");
        }
        
        // 设置创建时间和更新时间
        LocalDateTime now = LocalDateTime.now();
        review.setCreatedAt(now);
        review.setUpdatedAt(now);
        
        // 设置默认状态
        if (review.getStatus() == null) {
            review.setStatus("APPROVED");
        }
        
        // 保存评价
        Review savedReview = reviewRepository.save(review);
        
        logger.info("创建评价成功: {}", savedReview.getId());
        return savedReview;
    }

    @Override
    public Review getReviewById(Long id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("评价不存在: " + id));
    }

    @Override
    public List<Review> getReviewsByProductId(Long productId) {
        return reviewRepository.findByProductId(productId);
    }

    @Override
    public List<Review> getReviewsByUserId(Long userId) {
        return reviewRepository.findByUserId(userId);
    }
    
    @Override
    public List<Review> getReviewsByProductIdAndRating(Long productId, Integer rating) {
        if (rating == null || rating <= 0) {
            return reviewRepository.findByProductId(productId);
        }
        return reviewRepository.findByProductIdAndRatingGreaterThanEqual(productId, rating);
    }
    
    @Override
    public Double getAverageRatingByProductId(Long productId) {
        return reviewRepository.getAverageRatingByProductId(productId);
    }
    
    @Override
    public List<Review> getReviewsByStatus(String status) {
        return reviewRepository.findByStatus(status);
    }

    @Override
    @Transactional
    public Review updateReview(Review review) {
        // 检查评价是否存在
        Review existingReview = getReviewById(review.getId());
        
        // 更新评价内容
        existingReview.setRating(review.getRating());
        existingReview.setComment(review.getComment());
        existingReview.setImages(review.getImages());
        existingReview.setAnonymous(review.getAnonymous());
        existingReview.setStatus(review.getStatus());
        existingReview.setUpdatedAt(LocalDateTime.now());
        
        // 保存更新
        Review updatedReview = reviewRepository.save(existingReview);
        
        logger.info("更新评价成功: {}", updatedReview.getId());
        return updatedReview;
    }

    @Override
    @Transactional
    public void deleteReview(Long id) {
        Review review = getReviewById(id);
        
        reviewRepository.deleteById(id);
        logger.info("删除评价成功: {}", id);
    }
    
    @Override
    @Transactional
    public void reviewModeration(Long id, String status) {
        Review review = getReviewById(id);
        review.setStatus(status);
        review.setUpdatedAt(LocalDateTime.now());
        
        reviewRepository.save(review);
        logger.info("评价状态已更新: {}, 新状态: {}", id, status);
    }
    
    // 验证商品和用户存在性
    private void validateProductAndUser(Long productId, Long userId) {
        if (!productRepository.existsById(productId)) {
            throw new NoSuchElementException("商品不存在: " + productId);
        }
        
        if (!userRepository.existsById(userId)) {
            throw new NoSuchElementException("用户不存在: " + userId);
        }
    }
}