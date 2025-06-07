package com.backmore.secondhand_mall.dto;

import com.backmore.secondhand_mall.util.JsonUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ProductRequest {
    private String name;
    private Long sellerId;
    private Long categoryId;
    private Double price;
    private Double originalPrice;
    private Integer stock;
    private List<String> images;
    private String description;
    private String condition;
    private String usedDuration;
    private String brand;
    private Date purchaseDate;
    private Boolean faceToFace;
    private Boolean delivery;
    private String faceToFaceLocation;
    
    // 兼容前端字段名
    private Long seller_id;
    private Long category_id;
    private Double original_price;
    private String used_duration;
    private String face_to_face_location;
    private Boolean face_to_face;
    private SupportMethods supportMethods;
    // 可能的String形式的图片列表
    private String imagesAsString;
    
    public static class SupportMethods {
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

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getSellerId() {
        return sellerId != null ? sellerId : seller_id;
    }

    public void setSellerId(Long sellerId) {
        this.sellerId = sellerId;
    }
    
    public Long getSeller_id() {
        return seller_id;
    }

    public void setSeller_id(Long seller_id) {
        this.seller_id = seller_id;
    }

    public Long getCategoryId() {
        return categoryId != null ? categoryId : category_id;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }
    
    public Long getCategory_id() {
        return category_id;
    }

    public void setCategory_id(Long category_id) {
        this.category_id = category_id;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Double getOriginalPrice() {
        return originalPrice != null ? originalPrice : original_price;
    }

    public void setOriginalPrice(Double originalPrice) {
        this.originalPrice = originalPrice;
    }
    
    public Double getOriginal_price() {
        return original_price;
    }

    public void setOriginal_price(Double original_price) {
        this.original_price = original_price;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public List<String> getImages() {
        if (images != null) {
            return images;
        } else if (imagesAsString != null && !imagesAsString.isEmpty()) {
            try {
                // 如果imagesAsString是一个JSON字符串，尝试将其转换为List<String>
                ObjectMapper mapper = new ObjectMapper();
                return mapper.readValue(imagesAsString, new TypeReference<List<String>>() {});
            } catch (Exception e) {
                System.out.println("解析图片字符串失败: " + e.getMessage());
                List<String> list = new ArrayList<>();
                list.add(imagesAsString);
                return list;
            }
        }
        return new ArrayList<>();
    }

    public void setImages(List<String> images) {
        this.images = images;
    }
    
    public String getImagesAsString() {
        return imagesAsString;
    }
    
    public void setImagesAsString(String imagesAsString) {
        this.imagesAsString = imagesAsString;
        
        // 如果是JSON格式的字符串，尝试解析为List<String>
        if (imagesAsString != null && !imagesAsString.isEmpty()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                this.images = mapper.readValue(imagesAsString, new TypeReference<List<String>>() {});
            } catch (Exception e) {
                System.out.println("解析图片字符串失败: " + e.getMessage());
            }
        }
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
        return usedDuration != null ? usedDuration : used_duration;
    }

    public void setUsedDuration(String usedDuration) {
        this.usedDuration = usedDuration;
    }
    
    public String getUsed_duration() {
        return used_duration;
    }

    public void setUsed_duration(String used_duration) {
        this.used_duration = used_duration;
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
        if (faceToFace != null) return faceToFace;
        if (face_to_face != null) return face_to_face;
        return supportMethods != null ? supportMethods.getFaceToFace() : false;
    }

    public void setFaceToFace(Boolean faceToFace) {
        this.faceToFace = faceToFace;
    }
    
    public Boolean getFace_to_face() {
        return face_to_face;
    }

    public void setFace_to_face(Boolean face_to_face) {
        this.face_to_face = face_to_face;
    }

    public Boolean getDelivery() {
        if (delivery != null) return delivery;
        return supportMethods != null ? supportMethods.getDelivery() : true;
    }

    public void setDelivery(Boolean delivery) {
        this.delivery = delivery;
    }

    public String getFaceToFaceLocation() {
        return faceToFaceLocation != null ? faceToFaceLocation : face_to_face_location;
    }

    public void setFaceToFaceLocation(String faceToFaceLocation) {
        this.faceToFaceLocation = faceToFaceLocation;
    }
    
    public String getFace_to_face_location() {
        return face_to_face_location;
    }

    public void setFace_to_face_location(String face_to_face_location) {
        this.face_to_face_location = face_to_face_location;
    }
    
    public SupportMethods getSupportMethods() {
        return supportMethods;
    }

    public void setSupportMethods(SupportMethods supportMethods) {
        this.supportMethods = supportMethods;
    }
} 