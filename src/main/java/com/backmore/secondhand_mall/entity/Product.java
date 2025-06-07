package com.backmore.secondhand_mall.entity;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "product")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(nullable = false)
    private Double price;

    @Column(name = "original_price")
    private Double originalPrice;

    @Column(nullable = false, columnDefinition = "INT DEFAULT 1")
    private Integer stock;

    @Lob
    @Column(name = "main_image", columnDefinition = "LONGTEXT")
    private String mainImage;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String images;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "`condition`", length = 20)
    private String condition;

    @Column(name = "used_duration", length = 50)
    private String usedDuration;

    @Column(length = 50)
    private String brand;

    @Column(name = "purchase_date")
    @Temporal(TemporalType.DATE)
    private Date purchaseDate;

    @Column(name = "face_to_face", columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean faceToFace = false;

    @Column(columnDefinition = "TINYINT(1) DEFAULT 1")
    private Boolean delivery = true;

    @Column(name = "face_to_face_location", length = 255)
    private String faceToFaceLocation;

    @Column(columnDefinition = "INT DEFAULT 0")
    private Integer views = 0;

    @Column(columnDefinition = "INT DEFAULT 0")
    private Integer sales = 0;

    @Column(columnDefinition = "TINYINT(1) DEFAULT 1 COMMENT '0-下架 1-上架'")
    private Boolean status = true;

    @Column(name = "create_time", updatable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createTime;

    @Column(name = "update_time", columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updateTime;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public void setSellerId(Long sellerId) {
        this.sellerId = sellerId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Double getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(Double originalPrice) {
        this.originalPrice = originalPrice;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public String getMainImage() {
        return mainImage;
    }

    public void setMainImage(String mainImage) {
        this.mainImage = mainImage;
    }

    public String getImages() {
        return images;
    }

    public void setImages(String images) {
        this.images = images;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getUsedDuration() {
        return usedDuration;
    }

    public void setUsedDuration(String usedDuration) {
        this.usedDuration = usedDuration;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public Date getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(Date purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public Boolean getFaceToFace() {
        return faceToFace;
    }

    public void setFaceToFace(Boolean faceToFace) {
        this.faceToFace = faceToFace;
    }

    public Boolean getDelivery() {
        return delivery;
    }

    public void setDelivery(Boolean delivery) {
        this.delivery = delivery;
    }

    public String getFaceToFaceLocation() {
        return faceToFaceLocation;
    }

    public void setFaceToFaceLocation(String faceToFaceLocation) {
        this.faceToFaceLocation = faceToFaceLocation;
    }

    public Integer getViews() {
        return views;
    }

    public void setViews(Integer views) {
        this.views = views;
    }

    public Integer getSales() {
        return sales;
    }

    public void setSales(Integer sales) {
        this.sales = sales;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}