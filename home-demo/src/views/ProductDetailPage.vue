<template>
  <div class="product-detail-page">
    <!-- 使用共用导航组件 -->
    <NavBar />
    
    <div class="container" v-if="product">
      <div class="back-link">
        <router-link to="/">
          <i class="fas fa-arrow-left"></i> 返回首页
        </router-link>
      </div>
      
      <div class="product-container">
        <!-- 左侧：图片轮播 -->
        <div class="product-gallery">
          <div class="main-image">
            <el-carousel height="400px" indicator-position="outside" arrow="always" :autoplay="true">
              <el-carousel-item v-for="(image, index) in productImages" :key="index">
                <img :src="image" :alt="product.name" class="carousel-image" />
              </el-carousel-item>
            </el-carousel>
          </div>
          <div class="image-thumbnails">
            <div 
              v-for="(image, index) in productImages" 
              :key="index" 
              class="thumbnail"
              :class="{ active: currentImageIndex === index }"
              @click="currentImageIndex = index"
            >
              <img :src="image" :alt="`缩略图${index + 1}`" />
            </div>
          </div>
        </div>
        
        <!-- 右侧：商品信息 -->
        <div class="product-info-container">
          <h1 class="product-name">{{ product.name }}</h1>
          
          <div class="product-badges">
            <span class="badge hot" v-if="product.isHot">热卖</span>
            <span class="badge new" v-if="isNewProduct(product)">新品</span>
            <span class="badge category">{{ getCategoryName(product.categoryId) }}</span>
          </div>
          
          <div class="price-container">
            <div class="current-price">¥{{ product.price.toFixed(2) }}</div>
            <div class="original-price" v-if="product.originalPrice">
              原价: <span>¥{{ product.originalPrice.toFixed(2) }}</span>
            </div>
          </div>
          
          <div class="product-meta">
            <div class="meta-item">
              <span class="meta-label">商品状态:</span>
              <span class="meta-value">{{ product.stock > 0 ? '有货' : '缺货' }}</span>
            </div>
            <div class="meta-item">
              <span class="meta-label">库存:</span>
              <span class="meta-value" :class="{'low-stock': product.stock <= 3 && product.stock > 0}">
                {{ product.stock }}
              </span>
            </div>
            <div class="meta-item">
              <span class="meta-label">卖家:</span>
              <span class="meta-value seller">{{ product.seller }}</span>
            </div>
            <div class="meta-item">
              <span class="meta-label">发布时间:</span>
              <span class="meta-value">{{ formatPublishTime(product.publishTime) }}</span>
            </div>
          </div>
          
          <div class="product-description">
            <h3>商品描述</h3>
            <p>{{ product.description || '暂无描述' }}</p>
          </div>
          
          <div class="product-actions">
            <div class="quantity-selector">
              <button class="quantity-btn" @click="decrementQuantity" :disabled="quantity <= 1">-</button>
              <input type="number" v-model.number="quantity" min="1" :max="product.stock" />
              <button class="quantity-btn" @click="incrementQuantity" :disabled="quantity >= product.stock">+</button>
            </div>
            <button 
              class="add-to-cart-btn" 
              @click="addToCart" 
              :disabled="product.stock <= 0"
            >
              <i class="fas fa-shopping-cart"></i> 加入购物车
            </button>
            <button class="buy-now-btn" @click="buyNow" :disabled="product.stock <= 0">
              立即购买
            </button>
          </div>
          
          <div class="seller-contact">
            <button class="contact-btn" @click="contactSeller">
              <i class="fas fa-comment"></i> 联系卖家
            </button>
          </div>
        </div>
      </div>
      
      <!-- 商品详细信息和评价 -->
      <div class="product-tabs">
        <div class="tab-headers">
          <div 
            class="tab-header" 
            :class="{ active: activeTab === 'details' }"
            @click="activeTab = 'details'"
          >
            商品详情
          </div>
          <div 
            class="tab-header" 
            :class="{ active: activeTab === 'reviews' }"
            @click="activeTab = 'reviews'"
          >
            用户评价({{ reviews.length }})
          </div>
        </div>
        
        <div class="tab-content">
          <!-- 商品详情 -->
          <div v-if="activeTab === 'details'" class="product-details">
            <div class="detail-section">
              <h3>商品参数</h3>
              <table class="specs-table">
                <tr>
                  <td class="spec-name">品牌</td>
                  <td class="spec-value">{{ product.brand || '未知' }}</td>
                  <td class="spec-name">成色</td>
                  <td class="spec-value">{{ product.condition || '二手9成新' }}</td>
                </tr>
                <tr>
                  <td class="spec-name">购买日期</td>
                  <td class="spec-value">{{ product.purchaseDate || '未知' }}</td>
                  <td class="spec-name">使用时长</td>
                  <td class="spec-value">{{ product.usedDuration || '约1年' }}</td>
                </tr>
              </table>
            </div>
            <div class="detail-section">
              <h3>详细介绍</h3>
              <div class="detail-content">
                <p>{{ product.detailedDescription || product.description || '卖家很懒，没有留下详细介绍~' }}</p>
                <!-- 可以添加更多富文本内容 -->
              </div>
            </div>
          </div>
          
          <!-- 用户评价 -->
          <div v-if="activeTab === 'reviews'" class="product-reviews">
            <div v-if="reviews.length > 0">
              <div class="review-item" v-for="(review, index) in reviews" :key="index">
                <div class="review-header">
                  <div class="reviewer-info">
                    <div class="reviewer-avatar"></div>
                    <div class="reviewer-name">{{ review.username }}</div>
                  </div>
                  <div class="review-rating">
                    <div class="stars">
                      <i 
                        v-for="i in 5" 
                        :key="i" 
                        :class="[
                          'fas', 
                          i <= review.rating ? 'fa-star' : 'fa-star-o'
                        ]"
                      ></i>
                    </div>
                    <div class="review-date">{{ formatDate(review.date) }}</div>
                  </div>
                </div>
                <div class="review-content">
                  {{ review.content }}
                </div>
                <div v-if="review.images && review.images.length > 0" class="review-images">
                  <div 
                    v-for="(image, imgIndex) in review.images" 
                    :key="imgIndex" 
                    class="review-image"
                    @click="previewImage(image)"
                  >
                    <img :src="image" :alt="`评价图片${imgIndex + 1}`" />
                  </div>
                </div>
              </div>
            </div>
            <div v-else class="no-reviews">
              <i class="fas fa-comment-slash"></i>
              <p>暂无评价</p>
              <p class="sub-text">购买后可以来评价哦~</p>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 相关推荐 -->
      <div class="related-products">
        <h2 class="section-title">相关商品</h2>
        <div class="related-list">
          <div 
            v-for="item in relatedProducts" 
            :key="item.id" 
            class="related-item"
            @click="viewProduct(item.id)"
          >
            <div class="related-img">
              <img :src="item.image" :alt="item.name" />
            </div>
            <div class="related-info">
              <div class="related-name">{{ item.name }}</div>
              <div class="related-price">¥{{ item.price.toFixed(2) }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 商品不存在时的提示 -->
    <div v-else class="container not-found">
      <div class="not-found-content">
        <i class="fas fa-exclamation-circle"></i>
        <h2>商品不存在或已下架</h2>
        <router-link to="/" class="go-home-btn">返回首页</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useCartStore } from '@/stores/cart';
import NavBar from '@/components/NavBar.vue';

const route = useRoute();
const router = useRouter();
const cartStore = useCartStore();

// 商品ID
const productId = parseInt(route.params.id);

// 商品数据
const product = ref(null);
const currentImageIndex = ref(0);
const quantity = ref(1);
const activeTab = ref('details');

// 获取商品数据
onMounted(() => {
  const foundProduct = cartStore.products.find(p => p.id === productId);
  
  if (foundProduct) {
    // 为商品添加额外信息
    product.value = {
      ...foundProduct,
      categoryId: foundProduct.categoryId || Math.floor(Math.random() * 5) + 1,
      publishTime: foundProduct.publishTime || new Date(Date.now() - Math.random() * 90 * 24 * 60 * 60 * 1000).toISOString(),
      isHot: foundProduct.id === 1 || Math.random() > 0.7,
      description: foundProduct.description || '这是一个高品质的二手商品，几乎全新，欢迎咨询',
      seller: foundProduct.seller || `用户${Math.floor(Math.random() * 1000) + 1}`,
      originalPrice: foundProduct.id === 2 ? foundProduct.price * 1.2 : (Math.random() > 0.5 ? foundProduct.price * 1.3 : null),
      detailedDescription: '这是一款非常优质的二手商品，使用时间不长，功能完好，外观几乎全新。由于个人原因出售，价格优惠，欢迎有需要的同学联系购买。',
      condition: Math.random() > 0.5 ? '9成新' : '95成新',
      brand: ['Apple', 'Samsung', 'Huawei', 'Xiaomi', 'Sony'][Math.floor(Math.random() * 5)],
      usedDuration: ['3个月', '6个月', '1年', '1年半'][Math.floor(Math.random() * 4)],
      purchaseDate: new Date(Date.now() - Math.random() * 365 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
    };
  }
});

// 商品图片 (模拟多张图片)
const productImages = computed(() => {
  if (!product.value) return [];
  
  const baseImage = product.value.image;
  // 生成不同颜色的图片作为演示
  return [
    baseImage,
    baseImage.replace('ccc', 'eee'),
    baseImage.replace('ccc', 'ddd'),
    baseImage.replace('ccc', 'aaa')
  ];
});

// 模拟评价数据
const reviews = [
  {
    username: '用户101',
    rating: 5,
    date: new Date(Date.now() - 2 * 24 * 60 * 60 * 1000),
    content: '非常满意的一次购买，物品和描述一致，卖家很热情，发货也很快。',
    images: [
      'https://dummyimage.com/100x100/ccc/333',
      'https://dummyimage.com/100x100/eee/333'
    ]
  },
  {
    username: '用户239',
    rating: 4,
    date: new Date(Date.now() - 7 * 24 * 60 * 60 * 1000),
    content: '商品质量不错，就是发货稍微有点慢，总体还是比较满意的。'
  },
  {
    username: '用户587',
    rating: 5,
    date: new Date(Date.now() - 15 * 24 * 60 * 60 * 1000),
    content: '价格很实惠，比我想象中的要好，很划算！',
    images: [
      'https://dummyimage.com/100x100/ddd/333'
    ]
  }
];

// 相关商品推荐
const relatedProducts = computed(() => {
  if (!product.value || !product.value.categoryId) return [];
  
  // 过滤掉当前商品，并获取同类别的其他商品
  return cartStore.products
    .filter(p => p.id !== product.value.id && p.categoryId === product.value.categoryId)
    .slice(0, 4); // 最多显示4个相关商品
});

// 判断是否为新品（30天内发布）
function isNewProduct(product) {
  if (!product.publishTime) return false;
  const now = new Date();
  const publishDate = new Date(product.publishTime);
  const diffTime = Math.abs(now - publishDate);
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
  return diffDays <= 30;
}

// 根据分类ID获取分类名称
function getCategoryName(categoryId) {
  const categories = {
    1: '电子产品',
    2: '图书教材',
    3: '生活用品',
    4: '服装配饰',
    5: '运动户外'
  };
  
  return categories[categoryId] || '其他分类';
}

// 格式化发布时间
function formatPublishTime(timestamp) {
  if (!timestamp) return '未知时间';
  const now = new Date();
  const publishDate = new Date(timestamp);
  const diffTime = Math.abs(now - publishDate);
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
  
  if (diffDays < 1) return '今天';
  if (diffDays < 2) return '昨天';
  if (diffDays < 7) return `${diffDays}天前`;
  
  return `${publishDate.getFullYear()}-${publishDate.getMonth()+1}-${publishDate.getDate()}`;
}

// 格式化日期
function formatDate(date) {
  return new Date(date).toLocaleDateString();
}

// 调整购买数量
function incrementQuantity() {
  if (quantity.value < product.value.stock) {
    quantity.value++;
  }
}

function decrementQuantity() {
  if (quantity.value > 1) {
    quantity.value--;
  }
}

// 添加到购物车
function addToCart() {
  if (!product.value || product.value.stock <= 0) return;
  
  // 根据选择的数量多次调用添加购物车方法
  for (let i = 0; i < quantity.value; i++) {
    cartStore.addToCart(product.value.id);
  }
  
  alert(`已将 ${quantity.value} 个 ${product.value.name} 添加到购物车！`);
}

// 立即购买
function buyNow() {
  if (!product.value || product.value.stock <= 0) return;
  
  // 添加到购物车
  addToCart();
  
  // 跳转到结账页面
  router.push('/cart');
}

// 联系卖家
function contactSeller() {
  if (!product.value) return;
  
  router.push(`/messages?seller=${product.value.seller}`);
}

// 查看大图
function previewImage(image) {
  // 在实际应用中可以实现图片预览功能
  console.log('查看大图', image);
}

// 查看其他推荐商品
function viewProduct(id) {
  router.push(`/product/${id}`);
}
</script>

<style scoped>
/* 商品详情页整体样式 */
.product-detail-page {
  min-height: 100vh;
}

/* 内容容器样式 */
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

.back-link {
  margin-bottom: 20px;
}

.back-link a {
  color: #606266;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
}

.back-link a:hover {
  color: #409EFF;
}

.back-link i {
  margin-right: 5px;
}

/* 商品容器 */
.product-container {
  display: flex;
  gap: 30px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  padding: 30px;
  margin-bottom: 30px;
}

/* 图片轮播区 */
.product-gallery {
  width: 50%;
}

.main-image {
  width: 100%;
  margin-bottom: 15px;
  border-radius: 8px;
  overflow: hidden;
}

.carousel-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-thumbnails {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.thumbnail {
  width: 80px;
  height: 80px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
}

.thumbnail:hover {
  border-color: #409EFF;
}

.thumbnail.active {
  border: 2px solid #409EFF;
}

.thumbnail img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 商品信息区 */
.product-info-container {
  width: 50%;
}

.product-name {
  font-size: 24px;
  color: #303133;
  margin-top: 0;
  margin-bottom: 15px;
}

.product-badges {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 20px;
}

.badge {
  display: inline-block;
  padding: 2px 8px;
  font-size: 12px;
  border-radius: 4px;
}

.badge.hot {
  background-color: #f56c6c;
  color: white;
}

.badge.new {
  background-color: #67c23a;
  color: white;
}

.badge.category {
  background-color: #909399;
  color: white;
}

/* 价格区域 */
.price-container {
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 6px;
}

.current-price {
  color: #f56c6c;
  font-size: 28px;
  font-weight: bold;
}

.original-price {
  color: #909399;
  text-decoration: line-through;
  font-size: 14px;
  margin-top: 5px;
}

/* 商品元信息 */
.product-meta {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 15px;
  margin-bottom: 25px;
}

.meta-item {
  display: flex;
  align-items: center;
}

.meta-label {
  color: #909399;
  width: 80px;
}

.meta-value {
  color: #606266;
  flex: 1;
}

.meta-value.low-stock {
  color: #e6a23c;
  font-weight: bold;
}

.meta-value.seller {
  color: #409EFF;
  cursor: pointer;
}

/* 商品描述 */
.product-description {
  margin-bottom: 25px;
}

.product-description h3 {
  font-size: 16px;
  color: #303133;
  margin-bottom: 10px;
}

.product-description p {
  color: #606266;
  line-height: 1.6;
  margin: 0;
}

/* 购买操作区 */
.product-actions {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 20px;
}

.quantity-selector {
  display: flex;
  align-items: center;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  overflow: hidden;
}

.quantity-btn {
  background: none;
  border: none;
  width: 30px;
  height: 32px;
  font-size: 16px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.quantity-selector input {
  width: 50px;
  height: 32px;
  border: none;
  border-left: 1px solid #dcdfe6;
  border-right: 1px solid #dcdfe6;
  text-align: center;
}

.add-to-cart-btn, .buy-now-btn {
  height: 40px;
  border: none;
  border-radius: 4px;
  padding: 0 20px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.add-to-cart-btn {
  background-color: #ecf5ff;
  color: #409EFF;
  border: 1px solid #b3d8ff;
}

.add-to-cart-btn:hover {
  background-color: #409EFF;
  color: white;
}

.buy-now-btn {
  background-color: #f56c6c;
  color: white;
}

.buy-now-btn:hover {
  background-color: #ec5454;
}

.buy-now-btn:disabled, .add-to-cart-btn:disabled {
  background-color: #c0c4cc;
  color: white;
  cursor: not-allowed;
}

/* 联系卖家 */
.seller-contact {
  margin-top: 20px;
}

.contact-btn {
  border: 1px solid #dcdfe6;
  background: none;
  color: #606266;
  padding: 8px 15px;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.contact-btn:hover {
  color: #409EFF;
  border-color: #c6e2ff;
}

.contact-btn i {
  margin-right: 5px;
}

/* 商品详情和评价标签页 */
.product-tabs {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  margin-bottom: 30px;
}

.tab-headers {
  display: flex;
  border-bottom: 1px solid #dcdfe6;
}

.tab-header {
  padding: 15px 20px;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s;
  border-bottom: 2px solid transparent;
}

.tab-header:hover {
  color: #409EFF;
}

.tab-header.active {
  color: #409EFF;
  border-bottom-color: #409EFF;
}

.tab-content {
  padding: 25px;
}

/* 商品详情区域 */
.product-details {
  color: #606266;
}

.detail-section {
  margin-bottom: 30px;
}

.detail-section h3 {
  font-size: 16px;
  margin-bottom: 15px;
  color: #303133;
}

.specs-table {
  width: 100%;
  border-collapse: collapse;
}

.specs-table tr {
  border-bottom: 1px solid #ebeef5;
}

.specs-table tr:last-child {
  border-bottom: none;
}

.specs-table td {
  padding: 12px;
}

.spec-name {
  width: 100px;
  color: #909399;
  background-color: #f8f9fa;
}

.spec-value {
  color: #606266;
}

.detail-content {
  line-height: 1.8;
}

/* 评价区域 */
.product-reviews {
  color: #606266;
}

.review-item {
  border-bottom: 1px solid #ebeef5;
  padding: 20px 0;
}

.review-item:last-child {
  border-bottom: none;
}

.review-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 15px;
}

.reviewer-info {
  display: flex;
  align-items: center;
}

.reviewer-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background-color: #f0f2f5;
  margin-right: 10px;
}

.reviewer-name {
  font-weight: 500;
}

.review-rating {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.stars {
  color: #f7ba2a;
  margin-bottom: 5px;
}

.review-date {
  font-size: 12px;
  color: #909399;
}

.review-content {
  line-height: 1.6;
  margin-bottom: 15px;
}

.review-images {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.review-image {
  width: 70px;
  height: 70px;
  border-radius: 4px;
  overflow: hidden;
  cursor: pointer;
}

.review-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.no-reviews {
  text-align: center;
  padding: 40px 0;
  color: #909399;
}

.no-reviews i {
  font-size: 48px;
  margin-bottom: 15px;
  color: #dcdfe6;
}

.no-reviews .sub-text {
  font-size: 14px;
  margin-top: 5px;
}

/* 相关商品 */
.related-products {
  margin-bottom: 30px;
}

.section-title {
  font-size: 20px;
  color: #303133;
  margin-bottom: 20px;
  font-weight: 600;
}

.related-list {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 15px;
}

.related-item {
  background-color: white;
  border-radius: 6px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: all 0.3s;
}

.related-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 15px rgba(0, 0, 0, 0.15);
}

.related-img {
  height: 120px;
  overflow: hidden;
}

.related-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.related-info {
  padding: 10px;
}

.related-name {
  margin-bottom: 8px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.related-price {
  color: #f56c6c;
  font-weight: bold;
}

/* 商品不存在时的提示 */
.not-found {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}

.not-found-content {
  text-align: center;
  color: #909399;
}

.not-found-content i {
  font-size: 48px;
  margin-bottom: 15px;
}

.go-home-btn {
  display: inline-block;
  margin-top: 20px;
  padding: 10px 20px;
  background-color: #409EFF;
  color: white;
  border-radius: 4px;
  text-decoration: none;
}

/* 响应式样式 */
@media (max-width: 768px) {
  .product-container {
    flex-direction: column;
  }
  
  .product-gallery,
  .product-info-container {
    width: 100%;
  }
  
  .product-gallery {
    margin-bottom: 30px;
  }
  
  .related-list {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style> 