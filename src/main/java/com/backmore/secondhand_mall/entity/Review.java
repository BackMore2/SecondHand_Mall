package com.backmore.secondhand_mall.entity;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reviews")
@Data
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "rating", nullable = false)
    private Integer rating;

    @Column(name = "comment")
    private String comment;

    @Column(name = "images")
    private String images;

    @Column(name = "is_anonymous")
    private Boolean anonymous;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "create_time", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "update_time")
    private LocalDateTime updatedAt;
}