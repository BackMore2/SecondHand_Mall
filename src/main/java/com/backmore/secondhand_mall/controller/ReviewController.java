package com.backmore.secondhand_mall.controller;

import com.backmore.secondhand_mall.dto.ProductRatingDTO;
import com.backmore.secondhand_mall.entity.Review;
import com.backmore.secondhand_mall.entity.User;
import com.backmore.secondhand_mall.repository.UserRepository;
import com.backmore.secondhand_mall.service.ReviewService;
import com.backmore.secondhand_mall.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private UserRepository userRepository;

    /**
     * 创建新评价
     */
    @PostMapping
    public ResponseEntity<?> createReview(@RequestBody Review review) {
        try {
            // 从认证信息中获取用户ID，不再依赖前端传递
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("用户未认证");
            }

            // 获取认证的用户信息
            Object principal = authentication.getPrincipal();
            
            // 从认证信息中获取用户名
            String username = null;
            if (principal instanceof UserDetails) {
                username = ((UserDetails)principal).getUsername();
            } else {
                username = principal.toString();
            }
            
            // 通过用户名查找用户实体
            Optional<User> userOpt = userService.findByUsername(username);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("无法找到用户信息");
            }
            
            // 设置用户ID
            User user = userOpt.get();
            review.setUserId(user.getId());
            
            Review createdReview = reviewService.createReview(review);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdReview);
        } catch (IllegalStateException e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        } catch (NoSuchElementException e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "创建评价失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 根据ID获取评价
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getReviewById(@PathVariable Long id) {
        try {
            Review review = reviewService.getReviewById(id);
            return ResponseEntity.ok(review);
        } catch (NoSuchElementException e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    /**
     * 获取商品的所有评价，并附加用户名称和头像信息
     */
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<Map<String, Object>>> getReviewsByProductId(
            @PathVariable Long productId,
            @RequestParam(required = false) Integer minRating) {
        List<Review> reviews;
        if (minRating != null) {
            reviews = reviewService.getReviewsByProductIdAndRating(productId, minRating);
        } else {
            reviews = reviewService.getReviewsByProductId(productId);
        }
        
        // 将评价转换为带有用户信息的Map
        List<Map<String, Object>> enrichedReviews = new ArrayList<>();
        for (Review review : reviews) {
            Map<String, Object> enrichedReview = new HashMap<>();
            // 复制评价的所有属性
            enrichedReview.put("id", review.getId());
            enrichedReview.put("productId", review.getProductId());
            enrichedReview.put("userId", review.getUserId());
            enrichedReview.put("orderId", review.getOrderId());
            enrichedReview.put("rating", review.getRating());
            enrichedReview.put("comment", review.getComment());
            enrichedReview.put("images", review.getImages());
            enrichedReview.put("anonymous", review.getAnonymous());
            enrichedReview.put("status", review.getStatus());
            enrichedReview.put("createdAt", review.getCreatedAt());
            enrichedReview.put("updatedAt", review.getUpdatedAt());
            
            // 添加用户信息
            if (review.getAnonymous() == null || !review.getAnonymous()) {
                try {
                    Optional<User> userOpt = userRepository.findById(review.getUserId());
                    if (userOpt.isPresent()) {
                        User user = userOpt.get();
                        enrichedReview.put("username", user.getUsername());
                        enrichedReview.put("userAvatar", user.getAvatar());
                    } else {
                        enrichedReview.put("username", "用户" + review.getUserId());
                    }
                } catch (Exception e) {
                    enrichedReview.put("username", "用户" + review.getUserId());
                }
            } else {
                enrichedReview.put("username", "匿名用户");
            }
            
            enrichedReviews.add(enrichedReview);
        }
        
        return ResponseEntity.ok(enrichedReviews);
    }

    /**
     * 获取用户的所有评价
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Review>> getReviewsByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(reviewService.getReviewsByUserId(userId));
    }

    /**
     * 获取商品的评分信息
     */
    @GetMapping("/product/{productId}/rating")
    public ResponseEntity<ProductRatingDTO> getProductRating(@PathVariable Long productId) {
        Double averageRating = reviewService.getAverageRatingByProductId(productId);
        List<Review> reviews = reviewService.getReviewsByProductId(productId);
        
        ProductRatingDTO ratingInfo = new ProductRatingDTO(
            productId,
            averageRating,
            reviews.size()
        );
        
        return ResponseEntity.ok(ratingInfo);
    }

    /**
     * 根据状态获取评价
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Review>> getReviewsByStatus(@PathVariable String status) {
        return ResponseEntity.ok(reviewService.getReviewsByStatus(status));
    }

    /**
     * 更新评价
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateReview(@PathVariable Long id, @RequestBody Review review) {
        try {
            review.setId(id); // 确保ID匹配
            Review updatedReview = reviewService.updateReview(review);
            return ResponseEntity.ok(updatedReview);
        } catch (NoSuchElementException e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    /**
     * 删除评价
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteReview(@PathVariable Long id) {
        try {
            reviewService.deleteReview(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "评价已成功删除");
            return ResponseEntity.ok(response);
        } catch (NoSuchElementException e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    /**
     * 审核评价 (管理员功能)
     */
    @PutMapping("/{id}/moderate")
    public ResponseEntity<?> moderateReview(
            @PathVariable Long id,
            @RequestParam String status) {
        try {
            reviewService.reviewModeration(id, status);
            Map<String, String> response = new HashMap<>();
            response.put("message", "评价状态已更新为: " + status);
            return ResponseEntity.ok(response);
        } catch (NoSuchElementException e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
}