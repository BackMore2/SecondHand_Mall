<template>
  <div class="home-page">
    <!-- 使用共用导航组件 -->
    <NavBar />

    <!-- 分类快捷入口 -->
    <div class="category-section">
      <div class="container">
        <div class="section-header">
          <h2 class="section-title">商品分类</h2>
          <router-link to="/products/all" class="section-more">全部分类</router-link>
        </div>
        
        <div class="category-grid">
          <div 
            class="category-card" 
            v-for="category in categoryList" 
            :key="category.id"
            @click="selectCategory(category.id)"
            :class="{ active: selectedCategory === category.id }"
            :style="{ '--category-color': category.color }"
          >
            <div class="category-icon" :style="{ backgroundColor: category.color }">
              {{ category.name.slice(0, 1) }}
            </div>
            <h3 class="category-name">{{ category.name }}</h3>
            <span class="category-count">{{ getCategoryCount(category.id) }}件商品</span>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 商品筛选工具栏 -->
    <div class="filter-section">
      <div class="container">
        <div class="filter-container">
          <div class="filter-group">
            <label class="filter-label">价格区间：</label>
            <el-slider
              v-model="priceRange"
              range
              :min="0"
              :max="2000"
              :step="50"
              class="price-slider"
              :show-stops="false"
              :format-tooltip="value => `¥${value}`"
            ></el-slider>
            <span class="price-display">¥{{ priceRange[0] }} - ¥{{ priceRange[1] }}</span>
          </div>
          
          <div class="filter-group">
            <label class="filter-label">排序方式：</label>
            <div class="sort-options">
              <button 
                v-for="option in sortOptions" 
                :key="option.value"
                class="sort-btn" 
                :class="{ active: sortOption === option.value }"
                @click="sortOption = option.value; applySorting()"
              >
                {{ option.label }}
              </button>
            </div>
          </div>
          
          <div class="filter-group">
            <label class="stock-checkbox">
              <input type="checkbox" v-model="onlyInStock">
              <span class="checkbox-label">仅显示有货</span>
            </label>
          </div>
          
          <div class="filter-actions">
            <button class="reset-btn" @click="resetFilters">重置筛选</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 商品列表标题 -->
    <div class="product-section">
      <div class="container">
        <div class="section-header">
          <h2 class="section-title">{{ selectedCategory ? getCategoryNameById(selectedCategory) : '全部商品' }}</h2>
          <div class="section-info">找到 <span class="result-count">{{ filteredProducts.length }}</span> 个商品</div>
        </div>

    <!-- 商品列表 -->
        <div v-if="filteredProducts.length > 0" class="product-list">
          <div
            v-for="product in paginatedProducts"
          :key="product.id"
          class="product-card"
            @click="viewProductDetail(product.id)"
          >
            <div class="product-image-wrap">
              <img :src="product.image" class="product-image" :alt="product.name" />
              <div class="product-badges">
                <span class="badge hot" v-if="product.isHot">热卖</span>
                <span class="badge new" v-if="isNewProduct(product)">新品</span>
                <span class="badge category">{{ getCategoryNameById(product.categoryId) }}</span>
              </div>
              <div class="quick-actions">
                <button class="action-button view" @click.stop="viewProductDetail(product.id)" title="查看详情">
                  <i class="action-icon view-icon"></i>
                </button>
                <button 
                  class="action-button cart" 
                  @click.stop="cartStore.addToCart(product.id)"
            :disabled="product.stock <= 0"
                  :title="product.stock > 0 ? '加入购物车' : '已售罄'"
        >
                  <i class="action-icon cart-icon"></i>
                </button>
    </div>
            </div>
            <div class="product-info">
              <h3 class="product-name">{{ product.name }}</h3>
              <p class="product-desc">{{ product.description || '高品质商品，让生活更美好' }}</p>
              <div class="product-meta">
                <div class="seller-info">
                  <span class="seller-avatar"></span>
                  <span class="seller-name">{{ product.seller || '匿名用户' }}</span>
                </div>
                <span class="publish-time">{{ formatPublishTime(product.publishTime) }}</span>
              </div>
              <div class="price-info">
                <span class="price">¥{{ product.price }}</span>
                <span class="original-price" v-if="product.originalPrice">¥{{ product.originalPrice }}</span>
              </div>
              <div class="product-footer">
                <span class="stock-info" :class="{ 'low-stock': product.stock <= 3 && product.stock > 0 }">
                  库存: {{ product.stock }}
                  <span v-if="product.stock <= 3 && product.stock > 0" class="stock-alert">库存紧张!</span>
                </span>
              </div>
            </div>
          </div>
        </div>
        <div v-else class="empty-result">
          <div class="empty-container">
            <div class="empty-icon"></div>
            <h3 class="empty-title">暂无结果</h3>
            <p class="empty-desc">没有找到符合条件的商品</p>
            <button class="empty-button" @click="resetFilters">清除筛选条件</button>
          </div>
        </div>

        <!-- 分页 -->
        <div class="pagination-container" v-if="filteredProducts.length > pageSize">
          <div class="pagination">
            <button 
              class="page-btn prev" 
              :disabled="currentPage === 1"
              @click="handlePageChange(currentPage - 1)"
            >
              上一页
            </button>
            <button 
              v-for="page in totalPages" 
              :key="page" 
              class="page-btn number" 
              :class="{ active: currentPage === page }"
              @click="handlePageChange(page)"
            >
              {{ page }}
            </button>
            <button 
              class="page-btn next" 
              :disabled="currentPage === totalPages"
              @click="handlePageChange(currentPage + 1)"
            >
              下一页
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 推荐版块 -->
    <div class="recommendations-section">
      <div class="container">
        <div class="section-header">
          <h2 class="section-title">为您推荐</h2>
          <router-link to="/recommend" class="section-more">更多推荐</router-link>
        </div>
        
        <div class="recommendations-grid">
          <div class="recommendation-column" v-for="category in categoryList.slice(0, 3)" :key="category.id">
            <div class="recommendation-header" :style="{ backgroundColor: category.color }">
              <h3 class="recommendation-title">{{ category.name }}精选</h3>
              <router-link :to="`/products/${category.id}`" class="view-more">查看更多</router-link>
            </div>
            <div class="recommendation-items">
              <div 
                class="recommendation-item" 
                v-for="(product, index) in getRecommendedProductsByCategory(category.id, 4)" 
                :key="index" 
                @click="viewProductDetail(product.id)"
              >
                <div class="item-image-container">
                  <img :src="product.image" :alt="product.name" class="item-image" />
                </div>
                <div class="item-info">
                  <div class="item-name">{{ product.name }}</div>
                  <div class="item-price">¥{{ product.price }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 页脚 -->
    <footer class="site-footer">
      <div class="container">
        <div class="footer-columns">
          <div class="footer-column">
            <h4 class="footer-title">购物指南</h4>
            <ul class="footer-links">
              <li><a href="#">注册登录</a></li>
              <li><a href="#">购物流程</a></li>
              <li><a href="#">会员介绍</a></li>
              <li><a href="#">常见问题</a></li>
            </ul>
          </div>
          <div class="footer-column">
            <h4 class="footer-title">配送方式</h4>
            <ul class="footer-links">
              <li><a href="#">校内自提</a></li>
              <li><a href="#">当面交易</a></li>
              <li><a href="#">快递配送</a></li>
              <li><a href="#">配送范围</a></li>
            </ul>
          </div>
          <div class="footer-column">
            <h4 class="footer-title">支付方式</h4>
            <ul class="footer-links">
              <li><a href="#">货到付款</a></li>
              <li><a href="#">在线支付</a></li>
              <li><a href="#">校园交易</a></li>
              <li><a href="#">担保交易</a></li>
            </ul>
          </div>
          <div class="footer-column">
            <h4 class="footer-title">售后服务</h4>
            <ul class="footer-links">
              <li><a href="#">售后政策</a></li>
              <li><a href="#">退款说明</a></li>
              <li><a href="#">取消订单</a></li>
              <li><a href="#">投诉建议</a></li>
            </ul>
          </div>
          <div class="footer-column">
            <h4 class="footer-title">联系我们</h4>
            <p class="contact-info">客服电话：123-4567-8910</p>
            <p class="contact-info">邮箱：support@campus.com</p>
            <div class="social-links">
              <a href="#" class="social-link">微信</a>
              <a href="#" class="social-link">微博</a>
              <a href="#" class="social-link">QQ</a>
            </div>
          </div>
        </div>
        
        <div class="footer-bottom">
          <div class="copyright">© 2023 校园二手交易平台 版权所有</div>
          <div class="footer-nav">
            <a href="#">关于我们</a>
            <a href="#">用户协议</a>
            <a href="#">隐私政策</a>
            <a href="#">帮助中心</a>
          </div>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { useCartStore } from '@/stores/cart';
import { ref, computed, onMounted, onBeforeUnmount } from 'vue';
import { useRouter } from 'vue-router';
import NavBar from '@/components/NavBar.vue';

const router = useRouter();
const cartStore = useCartStore();

// 分类列表
const categoryList = [
  { id: 1, name: '教辅材料', color: '#f56c6c' },
  { id: 2, name: '生活用品', color: '#67c23a' },
  { id: 3, name: '数码产品', color: '#409eff' },
  { id: 4, name: '体育用品', color: '#e6a23c' },
  { id: 5, name: '其他', color: '#909399' }
];

// 排序选项
const sortOptions = [
  { label: '默认排序', value: 'default' },
  { label: '价格↑', value: 'priceAsc' },
  { label: '价格↓', value: 'priceDesc' },
  { label: '最新发布', value: 'newest' },
];

// 获取分类下的商品数量
const getCategoryCount = (categoryId) => {
  // 这里简单模拟一下数量，实际应用中可能需要从API获取
  return filteredProducts.value.filter(p => p.categoryId === categoryId).length;
};

// 筛选和排序状态
const selectedCategory = ref(null);
const priceRange = ref([0, 2000]);
const sortOption = ref('default');
const onlyInStock = ref(false);
const searchKeyword = ref('');
const currentPage = ref(1);
const pageSize = ref(8);

// 获取分类名称
const getCategoryNameById = (id) => {
  const category = categoryList.find(cat => cat.id === id);
  return category ? category.name : '未分类';
};

// 选择分类
const selectCategory = (categoryId) => {
  if (selectedCategory.value === categoryId) {
    selectedCategory.value = null; // 再次点击同一分类则取消选择
  } else {
    selectedCategory.value = categoryId;
  }
  currentPage.value = 1; // 重置分页
};

// 应用排序
const applySorting = () => {
  // 这里将在计算属性中处理排序逻辑
};

// 重置所有筛选条件
const resetFilters = () => {
  selectedCategory.value = null;
  priceRange.value = [0, 2000];
  sortOption.value = 'default';
  onlyInStock.value = false;
  searchKeyword.value = '';
  currentPage.value = 1;
};

// 查看商品详情
const viewProductDetail = (productId) => {
  router.push(`/product/${productId}`);
};

// 判断是否为新品（30天内发布）
const isNewProduct = (product) => {
  if (!product.publishTime) return false;
  const now = new Date();
  const publishDate = new Date(product.publishTime);
  const diffTime = Math.abs(now - publishDate);
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
  return diffDays <= 30;
};

// 格式化发布时间
const formatPublishTime = (timestamp) => {
  if (!timestamp) return '未知时间';
  const now = new Date();
  const publishDate = new Date(timestamp);
  const diffTime = Math.abs(now - publishDate);
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
  
  if (diffDays < 1) return '今天';
  if (diffDays < 2) return '昨天';
  if (diffDays < 7) return `${diffDays}天前`;
  
  return `${publishDate.getFullYear()}-${publishDate.getMonth()+1}-${publishDate.getDate()}`;
};

// 处理分页变化
const handlePageChange = (page) => {
  currentPage.value = page;
  window.scrollTo(0, 0); // 回到页面顶部
};

// 按分类获取推荐商品
const getRecommendedProductsByCategory = (categoryId, count) => {
  return cartStore.products
    .filter(product => product.categoryId === categoryId)
    .slice(0, count);
};

// 搜索产品功能
const searchProducts = () => {
  // 实际应用中可能需要从后端获取搜索结果
  currentPage.value = 1; // 重置分页
};

// 筛选后的商品列表
const filteredProducts = computed(() => {
  let result = [...cartStore.products];
  
  // 增强产品数据
  result = result.map(product => ({
    ...product,
    categoryId: product.categoryId || Math.floor(Math.random() * 5) + 1, // 随机分配分类用于演示
    publishTime: product.publishTime || new Date(Date.now() - Math.random() * 90 * 24 * 60 * 60 * 1000).toISOString(), // 随机设置发布时间
    isHot: product.id === 1 || Math.random() > 0.7, // 随机设置热卖标志
    description: product.description || '这是一个高品质的二手商品，几乎全新，欢迎咨询',
    seller: product.seller || `用户${Math.floor(Math.random() * 1000) + 1}`,
    originalPrice: product.id === 2 ? product.price * 1.2 : (Math.random() > 0.5 ? product.price * 1.3 : null)
  }));
  
  // 应用分类筛选
  if (selectedCategory.value) {
    result = result.filter(product => product.categoryId === selectedCategory.value);
  }
  
  // 应用价格范围筛选
  result = result.filter(product => 
    product.price >= priceRange.value[0] && product.price <= priceRange.value[1]
  );
  
  // 应用库存筛选
  if (onlyInStock.value) {
    result = result.filter(product => product.stock > 0);
  }
  
  // 应用关键词搜索
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase();
    result = result.filter(product => 
      product.name.toLowerCase().includes(keyword) || 
      (product.description && product.description.toLowerCase().includes(keyword))
    );
  }
  
  // 应用排序
  switch (sortOption.value) {
    case 'priceAsc':
      result.sort((a, b) => a.price - b.price);
      break;
    case 'priceDesc':
      result.sort((a, b) => b.price - a.price);
      break;
    case 'newest':
      result.sort((a, b) => new Date(b.publishTime) - new Date(a.publishTime));
      break;
    default:
      // 默认排序，可以是推荐算法或其他逻辑
      break;
  }
  
  return result;
});

// 当前页的商品
const paginatedProducts = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  return filteredProducts.value.slice(start, start + pageSize.value);
});

// 计算总页数
const totalPages = computed(() => {
  return Math.ceil(filteredProducts.value.length / pageSize.value);
});

// 模拟初始化数据 - 实际应用中可能从API获取
onMounted(() => {
  // 扩充商品模拟数据
  if (cartStore.products.length < 15) {
    const additionalProducts = Array.from({ length: 13 }).map((_, index) => ({
      id: cartStore.products.length + index + 1,
      name: `二手商品${cartStore.products.length + index + 1}`,
      price: Math.floor(Math.random() * 1000) + 99,
      image: `https://dummyimage.com/200x200/${Math.floor(Math.random() * 999)}/${Math.floor(Math.random() * 999)}`,
      stock: Math.floor(Math.random() * 10)
    }));
    
    // 更新商店中的商品列表
    cartStore.products.push(...additionalProducts);
  }
});
</script>

<style scoped>
.home-page {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  min-height: 100vh;
}

/* 分类部分样式 */
.category-section {
  padding: 40px 0;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
  border-bottom: 1px solid #eee;
  padding-bottom: 10px;
}

.section-title {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  margin: 0;
  position: relative;
  padding-left: 15px;
}

.section-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 5px;
  background-color: #4a99e9;
  border-radius: 2px;
}

.section-more {
  color: #666;
  font-size: 14px;
  text-decoration: none;
  transition: color 0.3s;
}

.section-more:hover {
  color: #4a99e9;
}

.category-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 20px;
}

.category-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
  border-radius: 8px;
  background-color: white;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  transition: all 0.3s;
  cursor: pointer;
  border: 2px solid transparent;
}

.category-card:hover {
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
  transform: translateY(-5px);
  border-color: var(--category-color, #4a99e9);
}

.category-card.active {
  border-color: var(--category-color, #4a99e9);
  background-color: rgba(var(--category-color, #4a99e9), 0.05);
}

.category-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: bold;
  color: white;
  margin-bottom: 15px;
}

.category-name {
  font-size: 16px;
  font-weight: bold;
  margin: 0 0 5px;
  color: #333;
}

.category-count {
  font-size: 12px;
  color: #999;
}

/* 筛选工具栏样式 */
.filter-section {
  padding: 20px 0;
  background-color: #f8f9fa;
}

.filter-container {
  background-color: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  align-items: center;
}

.filter-group {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 1;
  min-width: 200px;
}

.filter-label {
  font-size: 14px;
  color: #666;
  white-space: nowrap;
  font-weight: 500;
}

.price-slider {
  flex: 1;
}

.price-display {
  min-width: 100px;
  padding: 5px 10px;
  background-color: #f0f9ff;
  border-radius: 4px;
  color: #4a99e9;
  font-weight: bold;
  font-size: 13px;
  text-align: center;
}

.sort-options {
  display: flex;
  gap: 5px;
  flex-wrap: wrap;
}

.sort-btn {
  background-color: #f5f5f5;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  padding: 5px 15px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
  color: #666;
}

.sort-btn:hover {
  background-color: #eee;
}

.sort-btn.active {
  background-color: #4a99e9;
  color: white;
  border-color: #4a99e9;
}

.stock-checkbox {
  display: flex;
  align-items: center;
  gap: 5px;
  cursor: pointer;
}

.checkbox-label {
  font-size: 14px;
  color: #666;
}

.filter-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.reset-btn {
  background: none;
  border: 1px solid #ddd;
  border-radius: 4px;
  padding: 5px 15px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
  color: #666;
}

.reset-btn:hover {
  background-color: #f5f5f5;
  border-color: #ccc;
}

/* 商品列表标题部分 */
.product-section {
  padding: 30px 0;
}

.section-info {
  font-size: 14px;
  color: #999;
}

.result-count {
  color: #4a99e9;
  font-weight: bold;
}

/* 商品列表样式 */
.product-list {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 30px;
}

.product-card {
  background-color: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  transition: all 0.3s;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  height: 100%;
}

.product-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
}

.product-image-wrap {
  position: relative;
  height: 200px;
  overflow: hidden;
}

.product-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s;
}

.product-card:hover .product-image {
  transform: scale(1.05);
}

.product-badges {
  position: absolute;
  top: 10px;
  left: 10px;
  display: flex;
  flex-direction: column;
  gap: 5px;
  z-index: 2;
}

.badge {
  display: inline-block;
  padding: 3px 8px;
  font-size: 12px;
  color: white;
  border-radius: 4px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.2);
}

.badge.hot {
  background-color: #ff4d4f;
}

.badge.new {
  background-color: #52c41a;
}

.badge.category {
  background-color: rgba(0, 0, 0, 0.5);
}

.quick-actions {
  position: absolute;
  right: 10px;
  top: 10px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  opacity: 0;
  transform: translateX(10px);
  transition: all 0.3s ease;
}

.product-card:hover .quick-actions {
  opacity: 1;
  transform: translateX(0);
}

.action-button {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  background-color: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  transition: all 0.2s;
}

.action-button:hover {
  transform: scale(1.1);
}

.action-button.view {
  background-color: #4a99e9;
}

.action-button.cart {
  background-color: #ff9900;
}

.action-button:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

.action-icon {
  width: 18px;
  height: 18px;
  background-color: white;
}

.view-icon {
  mask: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='none' stroke='currentColor' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpath d='M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z'%3E%3C/path%3E%3Ccircle cx='12' cy='12' r='3'%3E%3C/circle%3E%3C/svg%3E") no-repeat center center / contain;
  -webkit-mask: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='none' stroke='currentColor' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpath d='M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z'%3E%3C/path%3E%3Ccircle cx='12' cy='12' r='3'%3E%3C/circle%3E%3C/svg%3E") no-repeat center center / contain;
}

.cart-icon {
  mask: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='none' stroke='currentColor' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3E%3Ccircle cx='9' cy='21' r='1'%3E%3C/circle%3E%3Ccircle cx='20' cy='21' r='1'%3E%3C/circle%3E%3Cpath d='M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6'%3E%3C/path%3E%3C/svg%3E") no-repeat center center / contain;
  -webkit-mask: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='none' stroke='currentColor' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3E%3Ccircle cx='9' cy='21' r='1'%3E%3C/circle%3E%3Ccircle cx='20' cy='21' r='1'%3E%3C/circle%3E%3Cpath d='M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6'%3E%3C/path%3E%3C/svg%3E") no-repeat center center / contain;
}

.product-info {
  padding: 15px;
  display: flex;
  flex-direction: column;
  flex: 1;
}

.product-name {
  font-size: 16px;
  font-weight: bold;
  margin: 0 0 8px;
  height: 44px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  color: #333;
}

.product-desc {
  color: #999;
  font-size: 12px;
  margin: 0 0 8px;
  height: 36px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.product-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  font-size: 12px;
  color: #999;
}

.seller-info {
  display: flex;
  align-items: center;
}

.seller-avatar {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background-color: #f0f0f0;
  display: inline-block;
  margin-right: 5px;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='%23999' stroke='%23999' stroke-width='1' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpath d='M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2'%3E%3C/path%3E%3Ccircle cx='12' cy='7' r='4'%3E%3C/circle%3E%3C/svg%3E");
  background-position: center;
  background-repeat: no-repeat;
  background-size: 60%;
}

.seller-name {
  max-width: 100px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.price-info {
  margin: 8px 0;
  display: flex;
  align-items: baseline;
}

.price {
  color: #ff4d4f;
  font-size: 18px;
  font-weight: bold;
}

.original-price {
  font-size: 14px;
  color: #999;
  text-decoration: line-through;
  margin-left: 8px;
}

.product-footer {
  margin-top: auto;
  padding-top: 10px;
  border-top: 1px dashed #f0f0f0;
}

.stock-info {
  font-size: 12px;
  color: #999;
}

.low-stock {
  color: #ff4d4f;
}

.stock-alert {
  display: inline-block;
  background-color: #fff0f0;
  color: #ff4d4f;
  padding: 1px 5px;
  border-radius: 3px;
  font-size: 10px;
  margin-left: 5px;
  border: 1px solid #ffccc7;
}

/* 空结果状态 */
.empty-result {
  padding: 40px 0;
  text-align: center;
}

.empty-container {
  background-color: white;
  padding: 40px;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  display: inline-block;
}

.empty-icon {
  width: 80px;
  height: 80px;
  margin: 0 auto 20px;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='none' stroke='%23ccc' stroke-width='1' stroke-linecap='round' stroke-linejoin='round'%3E%3Crect x='2' y='4' width='20' height='16' rx='2'%3E%3C/rect%3E%3Cpath d='M12 8v8'%3E%3C/path%3E%3Cpath d='M8 12h8'%3E%3C/path%3E%3C/svg%3E");
  background-position: center;
  background-repeat: no-repeat;
  background-size: contain;
}

.empty-title {
  font-size: 18px;
  color: #333;
  margin: 0 0 10px;
}

.empty-desc {
  color: #999;
  margin: 0 0 20px;
}

.empty-button {
  background-color: #4a99e9;
  color: white;
  border: none;
  padding: 8px 20px;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.empty-button:hover {
  background-color: #3a89d9;
}

/* 分页控件 */
.pagination-container {
  margin: 30px 0;
  display: flex;
  justify-content: center;
}

.pagination {
  display: flex;
  gap: 5px;
}

.page-btn {
  min-width: 32px;
  height: 32px;
  border: 1px solid #d9d9d9;
  background-color: white;
  color: #666;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
}

.page-btn:hover:not(:disabled) {
  color: #4a99e9;
  border-color: #4a99e9;
}

.page-btn.active {
  color: white;
  background-color: #4a99e9;
  border-color: #4a99e9;
}

.page-btn:disabled {
  color: #d9d9d9;
  cursor: not-allowed;
}

.page-btn.prev, .page-btn.next {
  padding: 0 10px;
}

/* 推荐区域样式 */
.recommendations-section {
  padding: 40px 0;
  background-color: #f8f9fa;
}

.recommendations-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.recommendation-column {
  background-color: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.recommendation-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 15px;
  color: white;
}

.recommendation-title {
  margin: 0;
  font-size: 16px;
  font-weight: bold;
}

.view-more {
  color: rgba(255, 255, 255, 0.8);
  font-size: 12px;
  text-decoration: none;
  transition: color 0.2s;
}

.view-more:hover {
  color: white;
}

.recommendation-items {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 1px;
  background-color: #f0f0f0;
}

.recommendation-item {
  padding: 10px;
  background-color: white;
  cursor: pointer;
  transition: all 0.2s;
}

.recommendation-item:hover {
  transform: scale(0.98);
  box-shadow: inset 0 0 10px rgba(0, 0, 0, 0.05);
}

.item-image-container {
  height: 80px;
  overflow: hidden;
  margin-bottom: 8px;
}

.item-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.4s;
  border-radius: 4px;
}

.recommendation-item:hover .item-image {
  transform: scale(1.1);
}

.item-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.item-name {
  font-size: 12px;
  color: #666;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 70%;
}

.item-price {
  font-size: 12px;
  color: #ff4d4f;
  font-weight: bold;
}

/* 页脚样式 */
.site-footer {
  background-color: #2c3e50;
  color: #ecf0f1;
  padding: 40px 0 20px;
}

.footer-columns {
  display: flex;
  flex-wrap: wrap;
  margin-bottom: 20px;
  gap: 24px;
  justify-content: center;
}

.footer-column {
  flex: 0 0 180px;
  min-width: 140px;
  margin-bottom: 10px;
}

.footer-title {
  font-size: 15px;
  margin: 0 0 10px;
  color: white;
  position: relative;
  padding-bottom: 6px;
}

.footer-title::after {
  content: '';
  position: absolute;
  left: 0;
  bottom: 0;
  width: 30px;
  height: 2px;
  background-color: #3498db;
}

.footer-links {
  list-style: none;
  padding: 0;
  margin: 0;
}

.footer-links li {
  margin-bottom: 5px;
}

.footer-links a {
  color: #bdc3c7;
  text-decoration: none;
  transition: color 0.2s;
  font-size: 13px;
}

.footer-links a:hover {
  color: white;
}

.contact-info {
  color: #bdc3c7;
  font-size: 13px;
  margin: 0 0 5px;
}

.social-links {
  display: flex;
  gap: 6px;
  margin-top: 10px;
}

.social-link {
  color: white;
  text-decoration: none;
  background-color: rgba(255, 255, 255, 0.1);
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  transition: all 0.2s;
}

.social-link:hover {
  background-color: rgba(255, 255, 255, 0.2);
}

.footer-bottom {
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  padding-top: 20px;
  display: flex;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 10px;
}

.copyright {
  color: #bdc3c7;
  font-size: 13px;
}

.footer-nav {
  display: flex;
  gap: 10px;
}

.footer-nav a {
  color: #bdc3c7;
  text-decoration: none;
  font-size: 13px;
  transition: color 0.2s;
}

.footer-nav a:hover {
  color: white;
}

/* 响应式调整 */
@media (max-width: 1200px) {
  .product-list {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 992px) {
  .recommendations-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .recommendation-column:last-child {
    display: none;
  }
  
  .category-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 768px) {
  .product-list {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .recommendations-grid {
    grid-template-columns: 1fr;
  }
  
  .category-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .footer-column {
    flex: 0 0 50%;
  }
}

@media (max-width: 576px) {
  .product-list {
    grid-template-columns: 1fr;
  }
  
  .pagination {
    flex-wrap: wrap;
    justify-content: center;
  }
  
  .footer-column {
    flex: 0 0 100%;
  }
  
  .footer-bottom {
    flex-direction: column;
    text-align: center;
  }
  
  .footer-nav {
    justify-content: center;
  }
}
</style>