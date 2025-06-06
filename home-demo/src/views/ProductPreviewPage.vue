<!--
  商品预览页面
  用于全屏预览编辑中的商品
-->
<template>
  <div class="product-preview-page">
    <!-- 导航栏 -->
    <NavBar />
    
    <div class="container">
      <div class="preview-header">
        <h2 class="preview-title">商品预览</h2>
        <div class="preview-actions">
          <button class="btn-back" @click="goBack">
            <i class="fas fa-arrow-left"></i> 返回编辑
          </button>
        </div>
      </div>
      
      <div class="preview-notice">
        <i class="fas fa-info-circle"></i>
        <span>这是商品预览模式，您可以查看商品在发布后的展示效果</span>
      </div>
      
      <div class="product-detail-container" v-if="product">
        <!-- 商品图片轮播 -->
        <div class="product-images">
          <div class="product-image-main">
            <img :src="currentImage" :alt="product.name" />
            
            <div class="image-nav">
              <button 
                class="nav-btn prev" 
                @click="prevImage" 
                :disabled="currentImageIndex === 0"
              >
                <i class="fas fa-chevron-left"></i>
              </button>
              <button 
                class="nav-btn next" 
                @click="nextImage" 
                :disabled="currentImageIndex === product.images.length - 1"
              >
                <i class="fas fa-chevron-right"></i>
              </button>
            </div>
          </div>
          
          <div class="product-thumbnails">
            <div 
              v-for="(image, index) in product.images" 
              :key="index" 
              class="thumbnail" 
              :class="{ active: currentImageIndex === index }"
              @click="setCurrentImage(index)"
            >
              <img :src="image" :alt="`缩略图${index + 1}`" />
            </div>
          </div>
        </div>
        
        <!-- 商品信息 -->
        <div class="product-info">
          <h1 class="product-name">{{ product.name }}</h1>
          
          <div class="product-price-container">
            <div class="price-current">¥{{ product.price.toFixed(2) }}</div>
            <div class="price-original" v-if="product.originalPrice">
              原价: <span>¥{{ product.originalPrice.toFixed(2) }}</span>
            </div>
          </div>
          
          <div class="product-meta">
            <div class="meta-item">
              <span class="meta-label">成色:</span>
              <span class="meta-value">{{ product.condition }}</span>
            </div>
            <div class="meta-item" v-if="product.usedDuration">
              <span class="meta-label">使用时长:</span>
              <span class="meta-value">{{ product.usedDuration }}</span>
            </div>
            <div class="meta-item" v-if="product.brand">
              <span class="meta-label">品牌:</span>
              <span class="meta-value">{{ product.brand }}</span>
            </div>
            <div class="meta-item" v-if="product.categoryId">
              <span class="meta-label">分类:</span>
              <span class="meta-value">{{ getCategoryName(product.categoryId) }}</span>
            </div>
          </div>
          
          <div class="product-trade-methods">
            <h3>交易方式</h3>
            <div class="trade-methods-list">
              <div class="trade-method" v-if="product.supportMethods.delivery">
                <i class="fas fa-truck"></i>
                <span>快递发货</span>
              </div>
              <div class="trade-method" v-if="product.supportMethods.faceToFace">
                <i class="fas fa-handshake"></i>
                <span>线下面交</span>
                <div class="face-to-face-location" v-if="product.faceToFaceLocation">
                  <i class="fas fa-map-marker-alt"></i>
                  <span>{{ product.faceToFaceLocation }}</span>
                </div>
              </div>
            </div>
          </div>
          
          <div class="product-buttons">
            <button class="btn-primary">立即购买</button>
            <button class="btn-secondary">
              <i class="fas fa-shopping-cart"></i>
              加入购物车
            </button>
          </div>
        </div>
      </div>
      
      <!-- 商品详情 -->
      <div class="product-detail-tabs" v-if="product">
        <div class="tabs-header">
          <div 
            class="tab-item" 
            :class="{ active: activeTab === 'description' }"
            @click="activeTab = 'description'"
          >
            商品详情
          </div>
        </div>
        
        <div class="tab-content">
          <div v-if="activeTab === 'description'" class="product-description">
            <p>{{ product.description || '暂无详细描述' }}</p>
          </div>
        </div>
      </div>
      
      <div class="preview-empty" v-else>
        <i class="fas fa-exclamation-circle"></i>
        <p>预览数据不存在，请返回商品编辑页面</p>
        <button class="btn-primary" @click="goBack">返回</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useProductStore } from '@/stores/product';
import NavBar from '@/components/NavBar.vue';

const router = useRouter();
const productStore = useProductStore();

// 商品数据
const product = ref(null);
const currentImageIndex = ref(0);
const activeTab = ref('description');

// 当前显示的图片
const currentImage = computed(() => {
  if (!product.value || !product.value.images || product.value.images.length === 0) {
    return 'https://via.placeholder.com/600x400?text=无图片';
  }
  return product.value.images[currentImageIndex.value];
});

// 加载预览商品数据
onMounted(() => {
  const previewData = localStorage.getItem('previewProduct');
  if (previewData) {
    try {
      product.value = JSON.parse(previewData);
      // 加载数据后立即清空localStorage，防止重复渲染
      localStorage.removeItem('previewProduct');
    } catch (e) {
      console.error('Failed to parse preview data:', e);
    }
  }
});

// 切换图片
function prevImage() {
  if (currentImageIndex.value > 0) {
    currentImageIndex.value--;
  }
}

function nextImage() {
  if (product.value && currentImageIndex.value < product.value.images.length - 1) {
    currentImageIndex.value++;
  }
}

function setCurrentImage(index) {
  currentImageIndex.value = index;
}

// 获取分类名称
function getCategoryName(categoryId) {
  const category = productStore.getCategoryList.find(c => c.id === categoryId);
  return category ? category.name : '未知分类';
}

// 返回编辑页面
function goBack() {
  window.close(); // 关闭预览窗口
}
</script>

<style scoped>
.product-preview-page {
  min-height: 100vh;
}

.container {
  position: relative;
  z-index: 1;
  background-color: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border-radius: 20px;
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.2);
  margin: 2rem auto;
  padding: 2rem;
  max-width: 1200px;
}

.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.preview-title {
  font-size: 24px;
  margin: 0;
  color: #303133;
}

.btn-back {
  background: #f5f7fa;
  color: #606266;
  border: 1px solid #dcdfe6;
  padding: 8px 15px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.btn-back:hover {
  color: #409EFF;
  border-color: #c6e2ff;
  background: #ecf5ff;
}

.preview-notice {
  background: #f0f9eb;
  color: #67c23a;
  padding: 10px 15px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.preview-notice i {
  margin-right: 10px;
  font-size: 16px;
}

.product-detail-container {
  display: flex;
  gap: 30px;
  margin-bottom: 30px;
}

.product-images {
  flex: 1;
  max-width: 500px;
}

.product-image-main {
  position: relative;
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 15px;
  height: 400px;
}

.product-image-main img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.image-nav {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  opacity: 0;
  transition: opacity 0.3s;
}

.product-image-main:hover .image-nav {
  opacity: 1;
}

.nav-btn {
  background: rgba(0, 0, 0, 0.5);
  color: white;
  border: none;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  transition: all 0.3s;
}

.nav-btn:hover {
  background: rgba(0, 0, 0, 0.7);
}

.nav-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.product-thumbnails {
  display: flex;
  gap: 10px;
  overflow-x: auto;
  padding-bottom: 5px;
}

.thumbnail {
  width: 80px;
  height: 80px;
  border-radius: 4px;
  overflow: hidden;
  cursor: pointer;
  border: 2px solid transparent;
  transition: all 0.3s;
}

.thumbnail.active {
  border-color: #409EFF;
}

.thumbnail img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-info {
  flex: 1;
}

.product-name {
  font-size: 24px;
  margin: 0 0 20px 0;
  color: #303133;
}

.product-price-container {
  display: flex;
  align-items: baseline;
  margin-bottom: 20px;
}

.price-current {
  font-size: 28px;
  font-weight: bold;
  color: #f56c6c;
  margin-right: 15px;
}

.price-original {
  color: #909399;
  font-size: 14px;
  text-decoration: line-through;
}

.product-meta {
  margin-bottom: 20px;
  padding: 15px;
  background: #f7f8fa;
  border-radius: 4px;
}

.meta-item {
  margin-bottom: 10px;
  display: flex;
}

.meta-label {
  width: 80px;
  color: #909399;
}

.meta-value {
  color: #606266;
  font-weight: 500;
}

.product-trade-methods {
  margin-bottom: 30px;
}

.product-trade-methods h3 {
  font-size: 16px;
  margin-bottom: 10px;
  color: #303133;
}

.trade-methods-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.trade-method {
  padding: 10px 15px;
  background: #f7f8fa;
  border-radius: 4px;
  display: flex;
  align-items: center;
}

.trade-method i {
  margin-right: 10px;
  color: #409EFF;
}

.face-to-face-location {
  margin-left: 20px;
  display: flex;
  align-items: center;
  color: #909399;
  font-size: 14px;
}

.face-to-face-location i {
  margin-right: 5px;
  color: #f56c6c;
}

.product-buttons {
  display: flex;
  gap: 15px;
}

.btn-primary {
  background: #f56c6c;
  color: white;
  border: none;
  padding: 12px 25px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
  font-weight: 500;
  flex: 1;
  transition: all 0.3s;
}

.btn-primary:hover {
  background: #f78989;
}

.btn-secondary {
  background: #409EFF;
  color: white;
  border: none;
  padding: 12px 25px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
  transition: all 0.3s;
}

.btn-secondary:hover {
  background: #66b1ff;
}

.product-detail-tabs {
  margin-top: 30px;
}

.tabs-header {
  display: flex;
  border-bottom: 1px solid #e4e7ed;
  margin-bottom: 20px;
}

.tab-item {
  padding: 10px 20px;
  cursor: pointer;
  position: relative;
  color: #606266;
  transition: all 0.3s;
}

.tab-item.active {
  color: #409EFF;
}

.tab-item.active::after {
  content: '';
  position: absolute;
  bottom: -1px;
  left: 0;
  right: 0;
  height: 2px;
  background: #409EFF;
}

.product-description {
  line-height: 1.8;
  color: #606266;
  white-space: pre-wrap;
}

.preview-empty {
  text-align: center;
  padding: 50px 0;
  color: #909399;
}

.preview-empty i {
  font-size: 60px;
  margin-bottom: 20px;
}

.preview-empty p {
  margin-bottom: 20px;
  font-size: 18px;
}
</style> 