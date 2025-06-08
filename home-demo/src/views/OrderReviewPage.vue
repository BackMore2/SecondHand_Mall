<template>
  <div class="order-review-page">
    <NavBar />
    
    <div class="container">
      <div class="back-link">
        <router-link :to="`/orders/${orderId}`">
          <i class="fas fa-arrow-left"></i> 返回订单详情
        </router-link>
      </div>
      
      <div class="page-header">
        <h2>商品评价</h2>
        <p>订单号: {{ orderInfo?.orderNumber || orderInfo?.id || orderId }}</p>
      </div>
      
      <div v-if="loading" class="loading-container">
        <div class="loading-spinner"></div>
        <p>正在加载数据...</p>
      </div>
      
      <div v-else-if="error" class="error-container">
        <i class="fas fa-exclamation-circle"></i>
        <p>{{ error }}</p>
        <button @click="fetchOrderData" class="retry-btn">重试</button>
      </div>
      
      <template v-else>
        <div class="review-items-container">
          <div v-for="(item, index) in orderItems" :key="index" class="review-item">
            <!-- 商品信息 -->
            <div class="product-info">
              <div class="product-image">
                <img 
                  :src="productDetails[item.productId]?.mainImage || item.image || 'https://via.placeholder.com/80'" 
                  :alt="productDetails[item.productId]?.name || `商品 #${item.productId}`"
                >
              </div>
              <div class="product-details">
                <h3>{{ productDetails[item.productId]?.name || `商品 #${item.productId}` }}</h3>
                <p class="product-price">¥{{ formatPrice(item.price) }}</p>
              </div>
            </div>
            
            <!-- 评价表单 -->
            <div class="review-form">
              <!-- 评分 -->
              <div class="rating-section">
                <label>评分</label>
                <div class="star-rating">
                  <span 
                    v-for="star in 5" 
                    :key="star" 
                    class="star" 
                    :class="{ active: star <= item.rating }"
                    @click="setItemRating(item, star)"
                  >
                    <i class="fas fa-star"></i>
                  </span>
                </div>
                <div class="rating-text">{{ getRatingText(item.rating) }}</div>
              </div>
              
              <!-- 评价内容 -->
              <div class="review-content">
                <label>评价内容</label>
                <textarea 
                  v-model="item.comment"
                  rows="3"
                  placeholder="请分享您对该商品的评价..."
                ></textarea>
              </div>
              
              <!-- 图片上传 -->
              <div class="image-upload-section">
                <label>上传图片 (最多3张)</label>
                <div class="upload-container">
                  <!-- 上传按钮 -->
                  <div 
                    class="upload-button" 
                    @click="triggerFileInput(index)"
                    v-if="!item.images || item.images.length < 3"
                  >
                    <i class="fas fa-camera"></i>
                    <span>添加图片</span>
                  </div>
                  
                  <!-- 预览区域 -->
                  <div 
                    v-for="(image, imgIndex) in item.images" 
                    :key="imgIndex" 
                    class="image-preview"
                  >
                    <img :src="image.url" :alt="`上传图片${imgIndex+1}`">
                    <div class="remove-image" @click="removeImage(item, imgIndex)">
                      <i class="fas fa-times"></i>
                    </div>
                  </div>
                  
                  <!-- 隐藏的文件输入 -->
                  <input 
                    type="file"
                    :ref="`fileInput-${index}`"
                    accept="image/*"
                    style="display: none"
                    @change="(e) => handleImageUpload(e, item)"
                    :multiple="!item.images || item.images.length < 2"
                  >
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 通用选项 -->
        <div class="common-options">
          <div class="anonymous-option">
            <input type="checkbox" id="anonymous" v-model="isAnonymous">
            <label for="anonymous">匿名评价全部商品</label>
          </div>
        </div>
        
        <!-- 提交按钮 -->
        <div class="submit-section">
          <button 
            @click="submitAllReviews" 
            class="submit-btn" 
            :disabled="submitting || !canSubmit"
          >
            {{ submitting ? '提交中...' : '提交全部评价' }}
          </button>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import NavBar from '@/components/NavBar.vue'
import * as orderApi from '@/api/order'
import * as reviewApi from '@/api/review'
import * as productApi from '@/api/product'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 获取路由参数
const orderId = Number(route.params.id)

// 状态变量
const orderInfo = ref(null)
const orderItems = ref([])
const productDetails = ref({})
const loading = ref(true)
const error = ref(null)
const submitting = ref(false)
const isAnonymous = ref(false)

// 检查是否所有商品都已评分
const canSubmit = computed(() => {
  if (!orderItems.value.length) return false
  return orderItems.value.every(item => item.rating > 0)
})

// 获取评分等级文本
function getRatingText(rating) {
  switch (rating) {
    case 1: return '非常差'
    case 2: return '较差'
    case 3: return '一般'
    case 4: return '不错'
    case 5: return '非常好'
    default: return '请选择评分'
  }
}

// 格式化价格
function formatPrice(price) {
  if (price === null || price === undefined) return '0.00'
  return Number(price).toFixed(2)
}

// 设置商品评分
function setItemRating(item, value) {
  item.rating = value
}

// 触发文件选择
function triggerFileInput(index) {
  const fileInput = document.querySelector(`input[ref="fileInput-${index}"]`)
  if (fileInput) fileInput.click()
}

// 处理图片上传
function handleImageUpload(event, item) {
  const files = event.target.files
  if (!files || !files.length) return
  
  // 初始化图片数组
  if (!item.images) item.images = []
  
  const maxFilesAllowed = 3 - item.images.length
  const filesToProcess = Array.from(files).slice(0, maxFilesAllowed)
  
  filesToProcess.forEach(file => {
    // 检查文件类型
    if (!['image/jpeg', 'image/jpg', 'image/png'].includes(file.type)) {
      alert('只支持jpg、jpeg、png格式的图片')
      return
    }
    
    // 检查文件大小
    if (file.size > 5 * 1024 * 1024) { // 5MB
      alert('图片大小不能超过5MB')
      return
    }
    
    // 创建文件预览
    const reader = new FileReader()
    reader.onload = (e) => {
      item.images.push({
        file: file,
        url: e.target.result
      })
    }
    reader.readAsDataURL(file)
  })
  
  // 清除文件输入，允许重新选择相同的文件
  event.target.value = ''
}

// 移除图片
function removeImage(item, index) {
  if (item.images) {
    item.images.splice(index, 1)
  }
}

// 处理数据结构嵌套问题
function unwrapNestedData(data) {
  if (!data) return null
  
  // 如果有data属性并且是对象，递归解析
  if (data.data && typeof data.data === 'object') {
    return unwrapNestedData(data.data)
  }
  
  return data
}

// 处理嵌套数组
function unwrapNestedArray(arr) {
  if (!Array.isArray(arr)) return []
  
  return arr.map(item => {
    // 如果item中有data属性且是对象，使用data中的内容
    if (item.data && typeof item.data === 'object') {
      return { ...item.data }
    }
    return item
  })
}

// 获取订单数据
async function fetchOrderData() {
  loading.value = true
  error.value = null
  
  try {
    // 获取订单详情
    const response = await orderApi.getOrderDetail(orderId)
    
    // 解包嵌套数据结构
    orderInfo.value = unwrapNestedData(response)
    
    // 提取订单项
    let items = []
    
    // 先检查orderInfo中是否包含订单项
    if (orderInfo.value?.items && Array.isArray(orderInfo.value.items)) {
      items = unwrapNestedArray(orderInfo.value.items)
    } 
    // 都没有则单独请求
    else {
      try {
        const itemsResponse = await orderApi.getOrderItems(orderId)
        
        if (Array.isArray(itemsResponse)) {
          items = unwrapNestedArray(itemsResponse)
        } else if (itemsResponse?.data && Array.isArray(itemsResponse.data)) {
          items = unwrapNestedArray(itemsResponse.data)
        }
      } catch (itemErr) {
        console.error('获取订单项失败:', itemErr)
      }
    }
    
    // 初始化每个商品的评价数据
    orderItems.value = items.map(item => ({
      ...item,
      rating: 0,
      comment: '',
      images: [],
    }))
    
    // 获取所有商品的详细信息
    const productIds = orderItems.value.map(item => item.productId).filter(id => id)
    
    // 过滤掉重复的产品ID
    const uniqueProductIds = [...new Set(productIds)]
    
    if (uniqueProductIds.length > 0) {
      // 并行获取所有产品详情
      await Promise.all(uniqueProductIds.map(fetchProductDetails))
    }
    
  } catch (err) {
    console.error('获取订单数据失败:', err)
    error.value = err.message || '获取订单数据失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

// 根据产品ID获取产品详情
async function fetchProductDetails(productId) {
  try {
    const response = await productApi.getProductById(productId)
    
    // 使用递归函数解包可能的嵌套结构
    const product = unwrapNestedData(response)
    
    // 添加到产品详情缓存
    productDetails.value[productId] = {
      id: productId,
      name: product.name || product.title || `商品 #${productId}`,
      description: product.description || '无描述',
      mainImage: product.mainImage || product.image || null,
      price: product.price
    }
  } catch (err) {
    console.error(`获取商品 #${productId} 详情失败:`, err)
  }
}

// 提交所有评价
async function submitAllReviews() {
  if (!canSubmit.value) {
    alert('请为所有商品评分后再提交')
    return
  }
  
  try {
    submitting.value = true
    
    // 准备所有评价数据并提交
    const reviewPromises = orderItems.value.map(item => {
      // 处理图片，将图片URL数组转换为JSON字符串
      const imageUrls = item.images ? item.images.map(img => img.url) : []
      
      // 简化的评价数据，只包含必要字段
      const reviewData = {
        productId: Number(item.productId),
        orderId: Number(orderId),
        rating: Number(item.rating),
        comment: item.comment || '',
        images: JSON.stringify(imageUrls),
        anonymous: isAnonymous.value,
        status: 'PUBLISHED'
      }
      
      // 直接调用API提交评价
      return fetch('http://localhost:8080/api/reviews', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        },
        body: JSON.stringify(reviewData)
      })
      .then(response => {
        if (!response.ok) throw new Error('提交评价失败')
        return response.json()
      })
    })
    
    // 等待所有评价提交完成
    await Promise.all(reviewPromises)
    
    alert('评价提交成功！')
    
    // 返回订单详情页
    router.push(`/orders/${orderId}`)
    
  } catch (error) {
    console.error('提交评价失败:', error)
    alert('提交评价失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

// 页面加载时获取订单数据
onMounted(() => {
  if (!orderId) {
    router.push('/orders')
    return
  }
  
  fetchOrderData()
})
</script>

<style scoped>
.order-review-page {
  min-height: 100vh;
  padding-bottom: 60px;
}

.container {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
  margin-top: 20px;
}

.back-link {
  margin-bottom: 20px;
}

.back-link a {
  color: #606266;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  font-size: 14px;
}

.back-link a:hover {
  color: #409EFF;
}

.back-link i {
  margin-right: 5px;
}

.page-header {
  text-align: center;
  margin-bottom: 30px;
}

.page-header h2 {
  margin-bottom: 5px;
  color: #303133;
}

.page-header p {
  color: #909399;
  font-size: 14px;
}

.loading-container,
.error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 50px;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  text-align: center;
}

.loading-spinner {
  border: 4px solid rgba(0, 0, 0, 0.1);
  border-left-color: #409EFF;
  border-radius: 50%;
  width: 40px;
  height: 40px;
  animation: spin 1s linear infinite;
  margin-bottom: 20px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.error-container i {
  font-size: 40px;
  color: #F56C6C;
  margin-bottom: 20px;
}

.retry-btn {
  background-color: #409EFF;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  cursor: pointer;
  margin-top: 20px;
}

.review-items-container {
  display: flex;
  flex-direction: column;
  gap: 30px;
}

.review-item {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  padding: 20px;
}

.product-info {
  display: flex;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #eee;
}

.product-image {
  width: 80px;
  height: 80px;
  margin-right: 15px;
  flex-shrink: 0;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 4px;
}

.product-details h3 {
  margin: 0 0 8px 0;
  font-size: 16px;
}

.product-price {
  color: #f56c6c;
  font-size: 14px;
  font-weight: 500;
}

.review-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.rating-section,
.review-content,
.image-upload-section {
  display: flex;
  flex-direction: column;
}

label {
  font-weight: 500;
  margin-bottom: 8px;
  color: #606266;
  font-size: 14px;
}

.star-rating {
  display: flex;
  gap: 8px;
}

.star {
  font-size: 20px;
  color: #dcdfe6;
  cursor: pointer;
  transition: transform 0.2s;
}

.star:hover {
  transform: scale(1.2);
}

.star.active {
  color: #f7ba2a;
}

.rating-text {
  margin-top: 5px;
  color: #909399;
  font-size: 12px;
}

textarea {
  width: 100%;
  padding: 10px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  outline: none;
  transition: border 0.3s;
  font-size: 14px;
  resize: none;
}

textarea:focus {
  border-color: #409EFF;
}

.upload-container {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 5px;
}

.upload-button {
  width: 80px;
  height: 80px;
  border: 1px dashed #dcdfe6;
  border-radius: 4px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #909399;
  transition: all 0.3s;
}

.upload-button:hover {
  color: #409EFF;
  border-color: #409EFF;
}

.upload-button i {
  font-size: 20px;
  margin-bottom: 5px;
}

.upload-button span {
  font-size: 12px;
}

.image-preview {
  width: 80px;
  height: 80px;
  position: relative;
  border-radius: 4px;
  overflow: hidden;
}

.image-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.remove-image {
  position: absolute;
  top: 0;
  right: 0;
  background-color: rgba(0, 0, 0, 0.5);
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: white;
}

.common-options {
  margin-top: 30px;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  padding: 20px;
}

.anonymous-option {
  display: flex;
  align-items: center;
  gap: 8px;
}

.submit-section {
  margin-top: 30px;
  display: flex;
  justify-content: center;
}

.submit-btn {
  background-color: #409EFF;
  color: white;
  border: none;
  padding: 12px 30px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
  transition: background-color 0.3s;
  min-width: 150px;
}

.submit-btn:not(:disabled):hover {
  background-color: #66b1ff;
}

.submit-btn:disabled {
  background-color: #a0cfff;
  cursor: not-allowed;
}
</style> 