<template>
  <div class="product-detail-page">
    <!-- 使用共用导航组件 -->
    <NavBar />
    
    <!-- 加载中 -->
    <div class="container loading-container" v-if="loading">
      <div class="loading-spinner">
        <i class="fas fa-spinner fa-spin"></i>
        <p>正在加载商品详情...</p>
      </div>
    </div>
    
    <div class="container" v-else-if="product">
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
              <span v-if="product.seller" class="meta-value seller">{{ product.seller }}</span>
              <span v-else class="meta-value seller">{{ sellerName }}</span>
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
            用户评价({{ productRating.reviewCount }})
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
            <!-- 加载中 -->
            <div v-if="reviewsLoading" class="reviews-loading">
              <i class="fas fa-spinner fa-spin"></i>
              <p>正在加载评价数据...</p>
            </div>
            
            <!-- 错误信息 -->
            <div v-else-if="reviewsError" class="reviews-error">
              <i class="fas fa-exclamation-circle"></i>
              <p>{{ reviewsError }}</p>
              <button class="retry-btn" @click="fetchReviews">重新加载</button>
            </div>
            
            <!-- 评价列表 -->
            <div v-else-if="reviews.length > 0" class="reviews-list">
              <div class="review-summary">
                <div class="review-count">
                  <span class="count-number">{{ productRating.reviewCount }}</span>
                  <span class="count-text">条评价</span>
                </div>
                <div class="review-stats">
                  <div class="stats-rating">
                    <span class="rating-average">{{ averageRating }}</span>
                    <div class="stars">
                      <i 
                        v-for="i in 5" 
                        :key="i" 
                        :class="['fas', i <= Math.round(averageRating) ? 'fa-star' : 'fa-star-o']"
                      ></i>
                    </div>
                  </div>
                </div>
              </div>
              
              <div class="review-item" v-for="(review, index) in reviews" :key="index">
                <div class="review-header">
                  <div class="reviewer-info">
                    <div class="reviewer-avatar">
                      <img :src="getUserAvatar(review)" alt="用户头像" class="avatar-image" />
                    </div>
                    <div class="reviewer-name">
                      {{ review.anonymous ? '匿名用户' : getUserName(review) }}
                    </div>
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
            
            <!-- 暂无评价 -->
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
        <h2>{{ error || '商品不存在或已下架' }}</h2>
        <router-link to="/" class="go-home-btn">返回首页</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, reactive } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useCartStore } from '@/stores/cart';
import { useProductStore } from '@/stores/product';
import { useUserStore } from '@/stores/user';
import NavBar from '@/components/NavBar.vue';
import * as productApi from '@/api/product';
import * as reviewApi from '@/api/review';

const route = useRoute();
const router = useRouter();
const cartStore = useCartStore();
const productStore = useProductStore();
const userStore = useUserStore();

// 商品ID
const productId = parseInt(route.params.id);

// 商品数据
const product = ref(null);
const currentImageIndex = ref(0);
const quantity = ref(1);
const activeTab = ref('details');
const loading = ref(false);
const error = ref(null);
const reviews = ref([]);
const reviewsLoading = ref(false);
const reviewsError = ref(null);
const productRating = ref({ averageRating: 0, reviewCount: 0 });

// 存储评价用户信息的map
const reviewUserInfoMap = reactive({});
// 卖家名称
const sellerName = ref('');

// 获取商品数据
onMounted(async () => {
  loading.value = true;
  error.value = null;
  
  try {
    // 先尝试从API获取商品详情
    const productData = await productApi.getProductById(productId);
    
    if (productData) {
      // 使用规范化函数处理获取的产品数据
      product.value = productStore.normalizeProduct(productData);
      
      // 增加商品浏览量
      productStore.incrementProductViews(productId);
      
      console.log('从API获取的商品详情:', product.value);
      
      // 获取商品评价和评分
      fetchReviews();
      fetchProductRating();
    } else {
      // 如果API没有返回数据，尝试从本地store获取
      const foundProduct = productStore.getProductById(productId) || 
                         cartStore.products.find(p => p.id === productId);
      
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
        
        // 获取模拟评价数据
        reviews.value = getMockReviews();
      } else {
        error.value = '商品不存在或已下架';
      }
    }
  } catch (err) {
    console.error('获取商品详情失败:', err);
    error.value = '获取商品详情失败';
    
    // 尝试从本地获取数据作为备选
    const foundProduct = productStore.getProductById(productId) || 
                       cartStore.products.find(p => p.id === productId);
    
    if (foundProduct) {
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
      
      // 获取模拟评价数据
      reviews.value = getMockReviews();
    }
  } finally {
    loading.value = false;
  }
  
  // 加载完商品后获取卖家和评论用户信息
  if (product.value) {
    await fetchSellerInfo();
  }
});

// 商品图片 (基于API返回的图片或模拟图片)
const productImages = computed(() => {
  if (!product.value) return [];
  
  // 如果有图片数组，使用它
  if (product.value.images && Array.isArray(product.value.images) && product.value.images.length > 0) {
    return product.value.images;
  }
  
  // 否则使用主图片
  const mainImage = product.value.mainImage || product.value.main_image || product.value.image;
  if (!mainImage) return [];
  
  // 生成不同颜色的图片作为演示
  return [
    mainImage,
    mainImage.replace('ccc', 'eee'),
    mainImage.replace('ccc', 'ddd'),
    mainImage.replace('ccc', 'aaa')
  ];
});

// 获取商品评价
const fetchReviews = async () => {
  if (!productId) return;
  
  reviewsLoading.value = true;
  reviewsError.value = null;
  
  try {
    // 从API获取评价数据
    const reviewsData = await reviewApi.getProductReviews(productId);
    
    if (Array.isArray(reviewsData) && reviewsData.length > 0) {
      // 对评价数据进行处理
      reviews.value = reviewsData.map(review => reviewApi.normalizeReview(review));
      console.log('获取到的评价数据:', reviews.value);
      
      // 获取所有评价的用户信息
      const userIds = reviews.value
        .filter(r => r.userId && !r.anonymous)
        .map(r => r.userId);
      
      // 获取不重复的用户ID列表
      const uniqueUserIds = [...new Set(userIds)];
      
      // 为每个用户获取信息
      await Promise.all(uniqueUserIds.map(fetchReviewUserInfo));
      
    } else {
      // 如果没有评价数据，使用模拟数据
      reviews.value = getMockReviews();
      
      // 为模拟数据添加用户信息
      reviews.value.forEach(review => {
        if (!review.userId) review.userId = Math.floor(Math.random() * 1000) + 1;
        fetchReviewUserInfo(review.userId);
      });
    }
  } catch (err) {
    console.error('获取评价失败:', err);
    reviewsError.value = '获取评价数据失败';
    // 使用模拟数据作为备选
    reviews.value = getMockReviews();
    
    // 为模拟数据添加用户信息
    reviews.value.forEach(review => {
      if (!review.userId) review.userId = Math.floor(Math.random() * 1000) + 1;
      fetchReviewUserInfo(review.userId);
    });
  } finally {
    reviewsLoading.value = false;
  }
};

// 获取商品评分数据
const fetchProductRating = async () => {
  if (!productId) return;
  
  try {
    // 从API获取评分数据
    const ratingData = await reviewApi.getProductRating(productId);
    
    if (ratingData) {
      productRating.value = {
        averageRating: ratingData.averageRating || 0,
        reviewCount: ratingData.reviewCount || 0
      };
      console.log('获取到的商品评分数据:', productRating.value);
    }
  } catch (err) {
    console.error('获取商品评分失败:', err);
    // 如果API失败，使用前端计算的评分作为备选
    productRating.value = {
      averageRating: calculateAverageRating(),
      reviewCount: reviews.value.length
    };
  }
};

// 获取模拟评价数据 (作为备选)
const getMockReviews = () => {
  return [
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
};

// 相关商品推荐
const relatedProducts = computed(() => {
  if (!product.value || !product.value.categoryId) return [];
  
  // 从productStore获取同类别的其他商品
  return productStore.products
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
  return productStore.getCategoryNameById(categoryId) || '其他分类';
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
async function addToCart() {
  if (!product.value || product.value.stock <= 0) return;
  
  try {
    // 显示加载状态
    const btnElement = document.querySelector('.add-to-cart-btn');
    const originalText = btnElement ? btnElement.innerHTML : '';
    
    if (btnElement) {
      btnElement.innerHTML = '<i class="fas fa-spinner fa-spin"></i> 正在添加...';
      btnElement.disabled = true;
    }
    
    // 直接使用数量参数一次性添加
    await cartStore.addToCart(product.value.id, quantity.value);
    
    // 成功提示
    alert(`已将 ${quantity.value} 个 ${product.value.name} 添加到购物车！`);
  } catch (error) {
    console.error('添加商品到购物车失败:', error);
    alert('添加失败，请稍后重试');
  } finally {
    // 恢复按钮状态
    const btnElement = document.querySelector('.add-to-cart-btn');
    if (btnElement) {
      setTimeout(() => {
        btnElement.innerHTML = '<i class="fas fa-shopping-cart"></i> 加入购物车';
        btnElement.disabled = false;
      }, 500);
    }
  }
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

// 前端计算平均评分（仅作为备选）
const calculateAverageRating = () => {
  if (!reviews.value || reviews.value.length === 0) return 0;
  
  const sum = reviews.value.reduce((total, review) => total + review.rating, 0);
  const avg = sum / reviews.value.length;
  
  // 保留一位小数
  return Math.round(avg * 10) / 10;
};

// 计算平均评分
const averageRating = computed(() => {
  return productRating.value.averageRating;
});

// 获取卖家信息
async function fetchSellerInfo() {
  if (product.value && product.value.sellerId) {
    try {
      // 这里可以通过API获取卖家信息
      // 由于我们可能没有直接的API，可以模拟一个或从已有数据中获取
      if (!product.value.seller) {
        sellerName.value = `ID:${product.value.sellerId}的用户`;
        // 如果有专门的接口获取用户信息可以在这里调用
      } else {
        sellerName.value = product.value.seller;
      }
    } catch (error) {
      console.error('获取卖家信息失败:', error);
    }
  }
}

// 获取评价中的用户信息
async function fetchReviewUserInfo(userId) {
  // 如果已经获取过该用户信息，直接返回缓存
  if (reviewUserInfoMap[userId]) {
    return reviewUserInfoMap[userId];
  }
  
  try {
    // 这里可以通过API获取用户信息
    // 模拟一个用户信息返回
    const userInfo = {
      id: userId,
      username: `用户${userId}`,
      avatar: `https://dummyimage.com/50x50/ccc/${userId.toString(16).padStart(3, '0')}`
    };
    
    reviewUserInfoMap[userId] = userInfo;
    return userInfo;
  } catch (error) {
    console.error(`获取用户 ${userId} 信息失败:`, error);
    return { username: `用户${userId}`, avatar: null };
  }
}

// 获取用户名称
function getUserName(review) {
  if (review.username) return review.username;
  if (review.userId && reviewUserInfoMap[review.userId]) {
    return reviewUserInfoMap[review.userId].username;
  }
  return `用户${review.userId || '未知'}`;
}

// 获取用户头像
function getUserAvatar(review) {
  if (review.userAvatar) return review.userAvatar;
  if (review.userId && reviewUserInfoMap[review.userId] && reviewUserInfoMap[review.userId].avatar) {
    return reviewUserInfoMap[review.userId].avatar;
  }
  return 'https://dummyimage.com/50x50/ccc/333'; // 默认头像
}
</script>

<style scoped>
/* 商品详情页整体样式 */
.product-detail-page {
  min-height: 100vh;
}

/* 加载状态样式 */
.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}

.loading-spinner {
  text-align: center;
}

.loading-spinner i {
  font-size: 3rem;
  color: #409EFF;
  margin-bottom: 1rem;
}

.loading-spinner p {
  font-size: 1.2rem;
  color: #606266;
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

/* 用户评价样式 */
.product-reviews {
  min-height: 300px;
}

.reviews-loading, 
.reviews-error, 
.no-reviews {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
  text-align: center;
}

.reviews-loading i,
.reviews-error i,
.no-reviews i {
  font-size: 3rem;
  margin-bottom: 1rem;
}

.reviews-loading i {
  color: #409EFF;
}

.reviews-error i {
  color: #F56C6C;
}

.reviews-error .retry-btn {
  margin-top: 15px;
  padding: 8px 15px;
  background-color: #409EFF;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.reviews-error .retry-btn:hover {
  background-color: #66b1ff;
}

.review-summary {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 8px;
  margin-bottom: 20px;
}

.review-count {
  display: flex;
  align-items: baseline;
}

.count-number {
  font-size: 2rem;
  font-weight: bold;
  color: #303133;
  margin-right: 5px;
}

.count-text {
  color: #606266;
}

.stats-rating {
  display: flex;
  align-items: center;
}

.rating-average {
  font-size: 1.5rem;
  font-weight: bold;
  color: #ff9800;
  margin-right: 10px;
}

.stars {
  color: #ff9800;
}

.review-item {
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  margin-bottom: 15px;
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
  width: 40px;
  height: 40px;
  border-radius: 50%;
  overflow: hidden;
  margin-right: 10px;
  background-color: #f0f0f0;
}

.reviewer-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.reviewer-name {
  font-weight: 500;
  color: #303133;
}

.review-rating {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.review-date {
  font-size: 0.8rem;
  color: #909399;
  margin-top: 5px;
}

.review-content {
  color: #303133;
  margin-bottom: 15px;
  line-height: 1.6;
}

.review-images {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.review-image {
  width: 80px;
  height: 80px;
  border-radius: 4px;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.3s;
}

.review-image:hover {
  transform: scale(1.05);
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
}

.not-found-content i {
  font-size: 4rem;
  color: #f56c6c;
  margin-bottom: 1rem;
}

.not-found-content h2 {
  font-size: 1.5rem;
  color: #606266;
  margin-bottom: 1.5rem;
}

.go-home-btn {
  display: inline-block;
  padding: 0.75rem 1.5rem;
  background-color: #409EFF;
  color: white;
  text-decoration: none;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.go-home-btn:hover {
  background-color: #66b1ff;
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