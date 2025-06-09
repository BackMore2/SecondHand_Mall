<template>
    <div class="search-page">
      <!-- 使用共用导航组件 -->
      <NavBar />
  
      <!-- 搜索结果标题 -->
      <div class="search-section">
        <div class="container">
          <div class="section-header">
            <h2 class="section-title">搜索结果: "{{ searchKeyword }}"</h2>
            <div class="section-info">找到 <span class="result-count">{{ filteredProducts.length }}</span> 个商品</div>
          </div>
  
          <!-- 筛选区域 -->
          <div class="filter-section">
            <div class="filter-container">
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
  
          <!-- 加载状态 -->
          <div v-if="isLoading" class="loading-container">
            <div class="loading-spinner"></div>
            <p class="loading-text">正在加载搜索结果...</p>
          </div>
          
          <!-- 错误状态 -->
          <div v-else-if="loadError" class="error-container">
            <div class="error-icon"></div>
            <h3 class="error-title">加载失败</h3>
            <p class="error-message">{{ loadError }}</p>
            <button class="retry-button" @click="searchProducts">重新加载</button>
          </div>
          
          <!-- 搜索结果列表 -->
          <div v-else-if="filteredProducts.length > 0" class="product-list">
            <div
              v-for="product in paginatedProducts"
              :key="product.id"
              class="product-card"
              @click="viewProductDetail(product.id)"
            >
              <div class="product-image-wrap">
                <img :src="getProductImage(product)" class="product-image" :alt="product.name" />
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
                    @click.stop="addToCart(product.id)"
                    :disabled="product.stock <= 0 || product.addingToCart"
                    :title="product.stock > 0 ? '加入购物车' : '已售罄'"
                  >
                    <i class="action-icon cart-icon" :class="{'spinning': product.addingToCart}"></i>
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
                  <div class="rating-info" v-if="product.averageRating > 0">
                    <span class="rating">{{ product.averageRating.toFixed(1) }}</span>
                    <span class="rating-count">({{ product.reviewCount }}条评价)</span>
                  </div>
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
              <h3 class="empty-title">未找到相关结果</h3>
              <p class="empty-desc">没有找到与"{{ searchKeyword }}"相关的商品</p>
              <router-link to="/" class="empty-button">返回首页</router-link>
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
    </div>
  </template>
  
  <script setup>
  import { ref, computed, onMounted, watch } from 'vue'
  import { useRoute, useRouter } from 'vue-router'
  import NavBar from '@/components/NavBar.vue'
  import { useProductStore } from '@/stores/product'
  
  const route = useRoute()
  const router = useRouter()
  const productStore = useProductStore()
  
  // 搜索关键词
  const searchKeyword = ref('')
  
  // 商品数据及筛选状态
  const products = ref([])
  const isLoading = ref(false)
  const loadError = ref(null)
  const sortOption = ref('default')
  const onlyInStock = ref(false)
  const currentPage = ref(1)
  const pageSize = ref(12)
  
  // 排序选项
  const sortOptions = [
    { label: '默认排序', value: 'default' },
    { label: '价格 低到高', value: 'price_asc' },
    { label: '价格 高到低', value: 'price_desc' },
    { label: '上架时间', value: 'time_desc' },
    { label: '销量优先', value: 'sales_desc' }
  ]
  
  // 获取URL搜索参数
  onMounted(() => {
    if (route.query.keyword) {
      searchKeyword.value = route.query.keyword
      searchProducts()
    }
  })
  
  // 监听路由参数变化
  watch(() => route.query.keyword, (newKeyword) => {
    if (newKeyword) {
      searchKeyword.value = newKeyword
      searchProducts()
      // 重置分页
      currentPage.value = 1
    }
  })
  
  // 搜索处理函数
  function searchProducts() {
    if (!searchKeyword.value) return
    
    isLoading.value = true
    loadError.value = null
    
    try {
      // 从productStore中获取商品并根据关键词过滤
      const storeProducts = productStore.availableProducts || []
      
      products.value = storeProducts.filter(p => 
        (p.name && p.name.toLowerCase().includes(searchKeyword.value.toLowerCase())) ||
        (p.description && p.description.toLowerCase().includes(searchKeyword.value.toLowerCase()))
      )
      isLoading.value = false
    } catch (error) {
      console.error('搜索商品出错:', error)
      loadError.value = '搜索商品失败，请重试'
      isLoading.value = false
    }
  }
  
  // 筛选逻辑
  const filteredProducts = computed(() => {
    let result = [...products.value]
    
    // 库存筛选
    if (onlyInStock.value) {
      result = result.filter(product => product.stock > 0)
    }
    
    // 排序逻辑
    switch (sortOption.value) {
      case 'price_asc':
        result.sort((a, b) => a.price - b.price)
        break
      case 'price_desc':
        result.sort((a, b) => b.price - a.price)
        break
      case 'time_desc':
        result.sort((a, b) => new Date(b.publishTime || b.createTime || 0) - new Date(a.publishTime || a.createTime || 0))
        break
      case 'sales_desc':
        // 假设有销量数据
        result.sort((a, b) => (b.salesCount || 0) - (a.salesCount || 0))
        break
      default:
        // 默认排序，可以按照综合权重
        // 这里简单实现为按热门程度排序
        result.sort((a, b) => {
          if (a.isHot && !b.isHot) return -1
          if (!a.isHot && b.isHot) return 1
          return 0
        })
    }
    
    return result
  })
  
  // 分页数据
  const totalPages = computed(() => Math.ceil(filteredProducts.value.length / pageSize.value))
  
  const paginatedProducts = computed(() => {
    const start = (currentPage.value - 1) * pageSize.value
    const end = start + pageSize.value
    return filteredProducts.value.slice(start, end)
  })
  
  // 分页处理函数
  function handlePageChange(page) {
    currentPage.value = page
    // 滚动到顶部
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }
  
  // 重置筛选条件
  function resetFilters() {
    sortOption.value = 'default'
    onlyInStock.value = false
    currentPage.value = 1
  }
  
  // 查看商品详情
  function viewProductDetail(productId) {
    router.push(`/product/${productId}`)
  }
  
  // 格式化发布时间
  function formatPublishTime(time) {
    if (!time) return '未知时间'
    const date = new Date(time)
    return `${date.getFullYear()}-${(date.getMonth()+1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`
  }
  
  // 商品图片处理
  function getProductImage(product) {
    // 优先使用images数组的第一张图片
    if (product.images && product.images.length > 0) {
      return product.images[0]
    }
    // 如果有单独的image字段，则使用它
    if (product.image) {
      return product.image
    }
    // 返回默认图片
    return 'https://via.placeholder.com/200x200?text=商品图片'
  }
  
  // 判断是否为新品（发布时间在30天内）
  function isNewProduct(product) {
    if (!product.publishTime && !product.createTime) return false
    const now = new Date()
    const publishDate = new Date(product.publishTime || product.createTime)
    const diffDays = Math.floor((now - publishDate) / (1000 * 60 * 60 * 24))
    return diffDays <= 30
  }
  
  // 获取分类名称
  function getCategoryNameById(categoryId) {
    return productStore.getCategoryNameById(categoryId) || '未分类'
  }
  
  // 添加到购物车
  function addToCart(productId) {
    // 这里应该调用添加到购物车的API
    console.log('添加商品到购物车:', productId)
  }
  
  // 应用排序
  function applySorting() {
    // 排序已通过计算属性实现
    // 这个函数主要用于响应UI事件
    currentPage.value = 1 // 重置到第一页
  }
  </script>
  
  <style scoped>
  .search-page {
    min-height: 100vh;
  }
  
  .container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 20px;
  }
  
  .section-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 20px;
  }
  
  .section-title {
    font-size: 1.8rem;
    color: #333;
    margin: 0;
  }
  
  .section-info {
    font-size: 14px;
    color: #606266;
  }
  
  .result-count {
    font-weight: bold;
    color: #409EFF;
  }
  
  /* 筛选区域样式 */
  .filter-section {
    margin-bottom: 20px;
    background-color: white;
    border-radius: 8px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  }
  
  .filter-container {
    padding: 15px;
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: 15px;
  }
  
  .filter-group {
    display: flex;
    align-items: center;
  }
  
  .filter-label {
    margin-right: 10px;
    color: #606266;
  }
  
  .sort-options {
    display: flex;
    gap: 10px;
  }
  
  .sort-btn {
    border: 1px solid #dcdfe6;
    background-color: white;
    color: #606266;
    padding: 6px 12px;
    border-radius: 4px;
    cursor: pointer;
    transition: all 0.3s;
  }
  
  .sort-btn:hover {
    border-color: #c6e2ff;
    color: #409EFF;
  }
  
  .sort-btn.active {
    border-color: #409EFF;
    color: #409EFF;
    background-color: #ecf5ff;
  }
  
  .stock-checkbox {
    display: flex;
    align-items: center;
    cursor: pointer;
  }
  
  .checkbox-label {
    margin-left: 5px;
    color: #606266;
  }
  
  .filter-actions {
    margin-left: auto;
  }
  
  .reset-btn {
    border: none;
    background-color: transparent;
    color: #909399;
    cursor: pointer;
  }
  
  .reset-btn:hover {
    color: #409EFF;
  }
  
  /* 加载和错误状态 */
  .loading-container, .error-container, .empty-result {
    padding: 40px 0;
    text-align: center;
  }
  
  .loading-spinner {
    display: inline-block;
    width: 50px;
    height: 50px;
    border: 3px solid rgba(64, 158, 255, 0.3);
    border-radius: 50%;
    border-top-color: #409EFF;
    animation: spin 1s ease-in-out infinite;
  }
  
  @keyframes spin {
    to { transform: rotate(360deg); }
  }
  
  .loading-text {
    margin-top: 15px;
    color: #606266;
  }
  
  .error-icon {
    display: inline-block;
    width: 60px;
    height: 60px;
    line-height: 60px;
    border-radius: 50%;
    background-color: #f56c6c;
    color: white;
    font-size: 30px;
    margin-bottom: 15px;
  }
  
  .error-title {
    font-size: 18px;
    color: #333;
    margin-bottom: 10px;
  }
  
  .error-message {
    color: #606266;
    margin-bottom: 15px;
  }
  
  .retry-button {
    background-color: #409EFF;
    color: white;
    border: none;
    padding: 8px 20px;
    border-radius: 4px;
    cursor: pointer;
  }
  
  /* 空结果样式 */
  .empty-container {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 40px 0;
  }
  
  .empty-icon {
    width: 80px;
    height: 80px;
    background-color: #f0f0f0;
    border-radius: 50%;
    margin-bottom: 20px;
  }
  
  .empty-title {
    font-size: 1.5rem;
    color: #333;
    margin-bottom: 10px;
  }
  
  .empty-desc {
    color: #909399;
    margin-bottom: 20px;
  }
  
  .empty-button {
    display: inline-block;
    background-color: #409EFF;
    color: white;
    padding: 8px 15px;
    border-radius: 4px;
    text-decoration: none;
    cursor: pointer;
    border: none;
  }
  
  /* 商品列表样式 */
  .product-list {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
    gap: 20px;
    margin-bottom: 30px;
  }
  
  .product-card {
    background-color: #fff;
    border-radius: 8px;
    overflow: hidden;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    transition: transform 0.3s ease, box-shadow 0.3s ease;
    cursor: pointer;
  }
  
  .product-card:hover {
    transform: translateY(-5px);
    box-shadow: 0 5px 15px 0 rgba(0, 0, 0, 0.15);
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
    transition: transform 0.5s ease;
  }
  
  .product-card:hover .product-image {
    transform: scale(1.05);
  }
  
  .product-badges {
    position: absolute;
    top: 10px;
    left: 10px;
    display: flex;
    flex-wrap: wrap;
    gap: 5px;
  }
  
  .badge {
    padding: 3px 6px;
    border-radius: 3px;
    font-size: 12px;
    color: white;
  }
  
  .badge.hot {
    background-color: #f56c6c;
  }
  
  .badge.new {
    background-color: #67c23a;
  }
  
  .badge.category {
    background-color: rgba(0, 0, 0, 0.5);
  }
  
  .quick-actions {
    position: absolute;
    top: 10px;
    right: 10px;
    display: flex;
    flex-direction: column;
    gap: 10px;
    opacity: 0;
    transition: opacity 0.3s ease;
  }
  
  .product-card:hover .quick-actions {
    opacity: 1;
  }
  
  .action-button {
    width: 36px;
    height: 36px;
    border-radius: 50%;
    background-color: white;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
    border: none;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: transform 0.3s ease;
  }
  
  .action-button:hover {
    transform: scale(1.1);
  }
  
  .action-button:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }
  
  .product-info {
    padding: 15px;
  }
  
  .product-name {
    font-size: 16px;
    font-weight: 600;
    color: #333;
    margin: 0 0 8px;
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    height: 40px;
  }
  
  .product-desc {
    color: #666;
    font-size: 14px;
    margin-bottom: 10px;
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    height: 40px;
  }
  
  .product-meta {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 10px;
    font-size: 12px;
    color: #999;
  }
  
  .seller-info {
    display: flex;
    align-items: center;
    gap: 5px;
  }
  
  .seller-avatar {
    width: 20px;
    height: 20px;
    border-radius: 50%;
    background-color: #eee;
  }
  
  .price-info {
    display: flex;
    align-items: center;
    margin-bottom: 10px;
    flex-wrap: wrap;
    gap: 8px;
  }
  
  .price {
    color: #f56c6c;
    font-size: 18px;
    font-weight: bold;
  }
  
  .original-price {
    color: #999;
    font-size: 14px;
    text-decoration: line-through;
  }
  
  .rating-info {
    margin-left: auto;
    display: flex;
    align-items: center;
    gap: 5px;
  }
  
  .rating {
    color: #e6a23c;
    font-weight: bold;
  }
  
  .rating-count {
    font-size: 12px;
    color: #999;
  }
  
  .product-footer {
    margin-top: 10px;
    font-size: 12px;
    color: #999;
  }
  
  .stock-info {
    display: inline-block;
  }
  
  .stock-info.low-stock {
    color: #e6a23c;
  }
  
  .stock-alert {
    color: #f56c6c;
    margin-left: 5px;
  }
  
  /* 分页样式 */
  .pagination-container {
    display: flex;
    justify-content: center;
    margin-top: 30px;
    padding-bottom: 20px;
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
    border-radius: 4px;
    font-size: 14px;
    cursor: pointer;
    transition: all 0.3s;
  }
  
  .page-btn:hover:not(:disabled) {
    color: #409EFF;
    border-color: #409EFF;
  }
  
  .page-btn.active {
    color: white;
    background-color: #409EFF;
    border-color: #409EFF;
  }
  
  .page-btn:disabled {
    cursor: not-allowed;
    color: #c0c4cc;
    background-color: #f4f4f5;
  }
  
  .prev, .next {
    padding: 0 10px;
  }
  </style>