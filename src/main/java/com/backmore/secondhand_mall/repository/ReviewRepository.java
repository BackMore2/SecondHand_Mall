
package com.backmore.secondhand_mall.repository;

import com.backmore.secondhand_mall.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    Review findByProductId(Long productId);

    List<Review> findByUserId(Long userId);
}