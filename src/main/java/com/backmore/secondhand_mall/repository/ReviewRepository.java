package com.backmore.secondhand_mall.repository;

import com.backmore.secondhand_mall.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    // 根据商品ID查询评价列表
    List<Review> findByProductId(Long productId);

    // 根据用户ID查询评价列表
    List<Review> findByUserId(Long userId);
    
    // 根据订单ID查询评价列表
    List<Review> findByOrderId(Long orderId);
    
    // 根据评分查询评价列表
    List<Review> findByRating(Integer rating);
    
    // 根据状态查询评价列表
    List<Review> findByStatus(String status);
    
    // 查询商品平均评分
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.productId = :productId")
    Double getAverageRatingByProductId(@Param("productId") Long productId);
    
    // 根据商品ID和评分查询评价列表
    List<Review> findByProductIdAndRatingGreaterThanEqual(Long productId, Integer rating);
    
    // 检查用户是否已经评价过某商品
    boolean existsByUserIdAndProductId(Long userId, Long productId);
}