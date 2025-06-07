package com.backmore.secondhand_mall.dto;

import com.backmore.secondhand_mall.entity.Product;
import com.backmore.secondhand_mall.util.JsonUtil;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;
import java.util.List;

public class ProductResponse {
    private Long id;
    private String name;
    private Long sellerId;
    private Long categoryId;
    private Double price;
    private Double originalPrice;
    private Integer stock;
    private List<String> images;
    private String mainImage;
    private String description;
    private String condition;
    private String usedDuration;
    private String brand;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date purchaseDate;
    
    private ProductSupportMethods supportMethods;
    private String faceToFaceLocation;
    private Integer views;
    private Integer sales;
    private String status;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    public static class ProductSupportMethods {
        private Boolean faceToFace;
        private Boolean delivery;

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
    }

    // 转换Product实体为ProductResponse对象
    public static ProductResponse fromEntity(Product product) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setSellerId(product.getSellerId());
        response.setCategoryId(product.getCategoryId());
        response.setPrice(product.getPrice());
        response.setOriginalPrice(product.getOriginalPrice());
        response.setStock(product.getStock());
        response.setMainImage(product.getMainImage());
        response.setDescription(product.getDescription());
        response.setCondition(product.getCondition());
        response.setUsedDuration(product.getUsedDuration());
        response.setBrand(product.getBrand());
        response.setPurchaseDate(product.getPurchaseDate());
        
        // 设置支持的交易方式
        ProductSupportMethods supportMethods = new ProductSupportMethods();
        supportMethods.setFaceToFace(product.getFaceToFace());
        supportMethods.setDelivery(product.getDelivery());
        response.setSupportMethods(supportMethods);
        
        response.setFaceToFaceLocation(product.getFaceToFaceLocation());
        response.setViews(product.getViews());
        response.setSales(product.getSales());
        
        // 设置商品状态
        response.setStatus(product.getStatus() ? "online" : "offline");
        
        response.setCreateTime(product.getCreateTime());
        response.setUpdateTime(product.getUpdateTime());
        
        // 解析图片JSON字符串为列表
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            response.setImages(JsonUtil.jsonToImages(product.getImages()));
        }
        
        return response;
    }

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

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public String getMainImage() {
        return mainImage;
    }

    public void setMainImage(String mainImage) {
        this.mainImage = mainImage;
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

    public ProductSupportMethods getSupportMethods() {
        return supportMethods;
    }

    public void setSupportMethods(ProductSupportMethods supportMethods) {
        this.supportMethods = supportMethods;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
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