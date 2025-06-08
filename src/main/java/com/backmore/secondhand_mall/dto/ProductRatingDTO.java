package com.backmore.secondhand_mall.dto;

public class ProductRatingDTO {
    private Long productId;
    private Double averageRating;
    private Integer reviewCount;

    public ProductRatingDTO() {
    }

    public ProductRatingDTO(Long productId, Double averageRating, Integer reviewCount) {
        this.productId = productId;
        this.averageRating = averageRating != null ? averageRating : 0.0;
        this.reviewCount = reviewCount != null ? reviewCount : 0;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating != null ? averageRating : 0.0;
    }

    public Integer getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(Integer reviewCount) {
        this.reviewCount = reviewCount != null ? reviewCount : 0;
    }
} 