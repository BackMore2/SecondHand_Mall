<template>
  <div class="order-detail-page">
    <!-- 使用共用导航组件 -->
    <NavBar />
    
    <div class="content-container">
      <div class="back-link">
        <router-link to="/orders">
          <i class="fas fa-arrow-left"></i> 返回我的订单
        </router-link>
      </div>
      
      <div v-if="loading" class="loading-container">
        <div class="loading-spinner"></div>
        <p>正在加载订单信息...</p>
      </div>
      
      <div v-else-if="error" class="error-container">
        <i class="fas fa-exclamation-circle"></i>
        <p>{{ error }}</p>
        <button @click="fetchOrderData" class="retry-btn">重试</button>
      </div>
      
      <template v-else-if="orderInfo">
        <div class="order-status-banner" :class="orderStatusClass">
          <div class="status-icon">
            <i :class="statusIcon"></i>
          </div>
          <div class="status-info">
            <h2>{{ orderInfo.status }}</h2>
            <p v-if="orderInfo.status === '待付款' || orderInfo.status === 'PENDING'">
              请尽快完成支付
            </p>
            <p v-else-if="orderInfo.status === '待发货' || orderInfo.status === 'PAID'">
              卖家正在处理您的订单，请耐心等待
            </p>
            <p v-else-if="orderInfo.status === '已发货' || orderInfo.status === 'SHIPPED'">
              您的包裹已在路上，请注意查收
            </p>
            <p v-else-if="orderInfo.status === '已完成' || orderInfo.status === 'COMPLETED'">
              交易已完成，感谢您的购买
            </p>
            <p v-else-if="orderInfo.status === '已取消' || orderInfo.status === 'CANCELED'">
              订单已取消
            </p>
          </div>
        </div>
        
        <!-- 订单基本信息 -->
        <div class="info-section order-base-info">
          <h3>订单信息</h3>
          <div class="info-row">
            <span class="info-label">订单编号：</span>
            <span class="info-value">{{ orderInfo.orderNumber || orderInfo.id || "未知编号" }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">下单时间：</span>
            <span class="info-value">{{ formatTime(orderInfo.createdAt || orderInfo.createTime || orderInfo.createDate) }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">支付方式：</span>
            <span class="info-value">{{ getPaymentMethodName(orderInfo.paymentMethod) }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">订单状态：</span>
            <span class="info-value">{{ orderInfo.status || "未知状态" }}</span>
          </div>
        </div>
        
        <!-- 收货信息 -->
        <div class="info-section delivery-info">
          <h3>收货信息</h3>
          <div class="info-row">
            <span class="info-label">收货地址：</span>
            <span class="info-value address">{{ orderInfo.shippingAddress || orderInfo.address || "未设置地址" }}</span>
          </div>
        </div>
        
        <!-- 商品信息 -->
        <div class="info-section product-info">
          <h3>商品信息</h3>
          <div class="product-list">
            <div v-for="(item, index) in orderItems" :key="index" class="product-item">
              <div class="product-img">
                <img :src="productDetails[item.productId]?.mainImage || item.image || 'https://via.placeholder.com/80'" 
                     :alt="productDetails[item.productId]?.name || `商品 #${item.productId}`" />
              </div>
              <div class="product-details">
                <h4 class="product-title">{{ productDetails[item.productId]?.name || `商品 #${item.productId}` }}</h4>
                <p class="product-desc">{{ productDetails[item.productId]?.description || item.description || '无描述' }}</p>
              </div>
              <div class="product-price">¥{{ formatPrice(item.price) }}</div>
              <div class="product-quantity">x{{ item.quantity }}</div>
              <div class="product-subtotal">¥{{ formatPrice(item.totalPrice || (item.price * item.quantity)) }}</div>
              <div class="product-actions">
                <button class="review-btn" @click="goToReview(item.productId)">
                  <i class="fas fa-comment"></i> 去评论
                </button>
              </div>
            </div>
          </div>
          <div class="order-total">
            <div class="total-row">
              <span>商品总计：</span>
              <span>¥{{ formatPrice(calculateSubtotal()) }}</span>
            </div>
            <div class="total-row">
              <span>运费：</span>
              <span>¥10.00</span>
            </div>
            <div class="total-row amount">
              <span>订单总计：</span>
              <span>¥{{ formatPrice(orderInfo.totalPrice || calculateSubtotal()) }}</span>
            </div>
          </div>
        </div>
        
        <!-- 操作按钮 -->
        <div class="action-buttons">
          <button v-if="orderInfo.status === '待付款' || orderInfo.status === 'PENDING'" 
                 class="btn pay-btn" @click="goToPay">
            去支付
          </button>
          <button v-if="orderInfo.status === '待付款' || orderInfo.status === 'PENDING'" 
                 class="btn cancel-btn" @click="cancelOrder">
            取消订单
          </button>
          <button v-if="orderInfo.status === '已发货' || orderInfo.status === 'SHIPPED'"
                 class="btn confirm-btn" @click="confirmReceive">
            确认收货
          </button>
          <button v-if="orderInfo.status === '已完成' || orderInfo.status === 'COMPLETED'"
                 class="btn review-all-btn" @click="reviewAllProducts">
            评价全部商品
          </button>
          <button class="btn contact-btn" @click="contactSeller">
            联系卖家
          </button>
        </div>
      </template>
      
      <div v-else class="empty-state">
        <i class="fas fa-box-open"></i>
        <p>订单信息不存在</p>
        <router-link to="/orders" class="link-btn">查看我的订单</router-link>
      </div>
    </div>
    

  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import NavBar from '../components/NavBar.vue'
import * as orderApi from '@/api/order' // 导入订单API
import * as productApi from '@/api/product' // 导入产品API

const route = useRoute()
const router = useRouter()
const orderId = Number(route.params.id)

// 状态变量
const orderInfo = ref(null)
const orderItems = ref([])
const productDetails = ref({}) // 存储商品详情
const loading = ref(true)
const error = ref(null)


// 支付方式映射
const paymentMethods = {
  'alipay': '支付宝',
  'wechat': '微信支付',
  'card': '银行卡支付',
  'ALIPAY': '支付宝',
  'WECHAT': '微信支付',
  'CARD': '银行卡支付'
}

// 获取支付方式名称
const getPaymentMethodName = (methodId) => {
  return paymentMethods[methodId] || methodId || '未选择'
}

// 计算订单状态对应的样式类
const orderStatusClass = computed(() => {
  const status = orderInfo.value?.status
  
  if (!status) return ''
  
  const statusLower = status.toLowerCase()
  if (statusLower.includes('pend') || statusLower.includes('待付款')) {
    return 'status-pending-payment'
  } else if (statusLower.includes('paid') || statusLower.includes('待发货')) {
    return 'status-pending-shipment'
  } else if (statusLower.includes('ship') || statusLower.includes('已发货')) {
    return 'status-shipped'
  } else if (statusLower.includes('complete') || statusLower.includes('已完成')) {
    return 'status-completed'
  } else if (statusLower.includes('cancel') || statusLower.includes('已取消')) {
    return 'status-canceled'
  }
  return ''
})

// 计算订单状态对应的图标
const statusIcon = computed(() => {
  const status = orderInfo.value?.status
  
  if (!status) return 'fas fa-question-circle'
  
  const statusLower = status.toLowerCase()
  if (statusLower.includes('pend') || statusLower.includes('待付款')) {
    return 'fas fa-credit-card'
  } else if (statusLower.includes('paid') || statusLower.includes('待发货')) {
    return 'fas fa-box'
  } else if (statusLower.includes('ship') || statusLower.includes('已发货')) {
    return 'fas fa-shipping-fast'
  } else if (statusLower.includes('complete') || statusLower.includes('已完成')) {
    return 'fas fa-check-circle'
  } else if (statusLower.includes('cancel') || statusLower.includes('已取消')) {
    return 'fas fa-times-circle'
  }
  return 'fas fa-question-circle'
})

// 格式化时间
function formatTime(time) {
  if (!time) return '未知'
  try {
    return new Date(time).toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    })
  } catch (e) {
    return time.toString()
  }
}

// 格式化价格
function formatPrice(price) {
  if (price === null || price === undefined) return '0.00'
  return Number(price).toFixed(2)
}

// 计算商品总价
function calculateSubtotal() {
  if (!orderItems.value || orderItems.value.length === 0) return 0
  return orderItems.value.reduce((sum, item) => {
    const itemTotal = item.totalPrice || (item.price * item.quantity)
    return sum + Number(itemTotal)
  }, 0)
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

// 递归处理嵌套数据结构，解决无限嵌套问题
function unwrapNestedData(data) {
  if (!data) return null;
  
  // 如果有data属性并且是对象，递归解析
  if (data.data && typeof data.data === 'object') {
    return unwrapNestedData(data.data);
  }
  
  return data;
}

// 递归处理嵌套数组，解决item无限嵌套问题
function unwrapNestedArray(arr) {
  if (!Array.isArray(arr)) return [];
  
  return arr.map(item => {
    // 如果item中有data属性且是对象，使用data中的内容
    if (item.data && typeof item.data === 'object') {
      return { ...item.data };
    }
    return item;
  });
}

// 获取订单数据
async function fetchOrderData() {
  loading.value = true
  error.value = null
  
  try {
    console.log(`正在获取订单 #${orderId} 的详细信息...`)
    // 获取订单详情
    const response = await orderApi.getOrderDetail(orderId)
    
    // 解包嵌套数据结构
    orderInfo.value = unwrapNestedData(response)
    console.log('处理后的订单信息:', orderInfo.value)
    
    // 提取订单项
    let items = []
    
    // 先检查orderInfo中是否包含订单项
    if (orderInfo.value.items && Array.isArray(orderInfo.value.items)) {
      items = unwrapNestedArray(orderInfo.value.items)
      console.log('从订单中获取并处理订单项:', items)
    } 
    // 都没有则单独请求
    else {
      console.log('单独获取订单项...')
      try {
        const itemsResponse = await orderApi.getOrderItems(orderId)
        
        if (Array.isArray(itemsResponse)) {
          items = unwrapNestedArray(itemsResponse)
        } else if (itemsResponse && itemsResponse.data && Array.isArray(itemsResponse.data)) {
          items = unwrapNestedArray(itemsResponse.data)
        }
        
        console.log('获取并处理后的订单项:', items)
      } catch (itemErr) {
        console.error('获取订单项失败:', itemErr)
      }
    }
    
    orderItems.value = items || []
    
    // 获取所有商品的详细信息
    const productIds = orderItems.value.map(item => item.productId).filter(id => id)
    
    // 过滤掉重复的产品ID
    const uniqueProductIds = [...new Set(productIds)]
    
    if (uniqueProductIds.length > 0) {
      // 并行获取所有产品详情
      await Promise.all(uniqueProductIds.map(productId => fetchProductDetails(productId)))
    }
    
  } catch (err) {
    console.error('获取订单数据失败:', err)
    error.value = err.message || '获取订单数据失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

// 前往支付页面
function goToPay() {
  router.push(`/order/pay/${orderId}`)
}

// 取消订单
async function cancelOrder() {
  if (!confirm('确定要取消此订单吗？')) return
  
  try {
    loading.value = true
    await orderApi.cancelOrder(orderId)
    alert('订单已取消')
    
    // 刷新订单数据
    await fetchOrderData()
  } catch (err) {
    alert(`取消订单失败: ${err.message || '未知错误'}`)
    console.error('取消订单失败:', err)
  } finally {
    loading.value = false
  }
}

// 确认收货
async function confirmReceive() {
  if (!confirm('确认已收到商品？')) return
  
  try {
    loading.value = true
    await orderApi.confirmOrder(orderId)
    alert('已确认收货')
    
    // 刷新订单数据
    await fetchOrderData()
  } catch (err) {
    alert(`确认收货失败: ${err.message || '未知错误'}`)
    console.error('确认收货失败:', err)
  } finally {
    loading.value = false
  }
}

// 前往评论单个商品
function goToReview(productId) {
  router.push(`/product/${productId}/review?orderId=${orderId}`)
}

// 评价所有商品
function reviewAllProducts() {
  router.push(`/order/${orderId}/review`)
}

// 联系卖家
function contactSeller() {
  // 这里可以添加联系卖家的逻辑
  alert('联系卖家功能尚未实现')
}

// 页面加载时获取订单数据
onMounted(() => {
  if (!orderId) {
    error.value = '未提供有效的订单ID'
    loading.value = false
    return
  }
  
  fetchOrderData()
})
</script>

<style scoped>
.order-detail-page {
  min-height: 100vh;
  background-color: transparent;
}

.content-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
  background-color: transparent;
  margin-top: 20px;
  margin-bottom: 40px;
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

.order-status-banner {
  display: flex;
  align-items: center;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 30px;
  color: #fff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.status-pending-payment {
  background: linear-gradient(135deg, #ff9a9e 0%, #fad0c4 100%);
}

.status-pending-shipment {
  background: linear-gradient(135deg, #a6c0fe 0%, #f68084 100%);
}

.status-shipped {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.status-completed {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.status-canceled {
  background: linear-gradient(135deg, #606c88 0%, #3f4c6b 100%);
}

.status-icon {
  font-size: 2.5rem;
  margin-right: 20px;
}

.status-info h2 {
  margin: 0 0 10px 0;
  font-size: 1.5rem;
}

.status-info p {
  margin: 0;
  opacity: 0.9;
}

.info-section {
  margin-bottom: 30px;
  padding: 20px;
  border-radius: 8px;
  background-color: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(10px);
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.info-section h3 {
  margin-bottom: 15px;
  font-size: 18px;
  color: #555;
  position: relative;
  padding-left: 12px;
}

.info-section h3::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 16px;
  background-color: #409EFF;
  border-radius: 2px;
}

.info-row {
  display: flex;
  margin-bottom: 10px;
}

.info-label {
  width: 100px;
  color: #909399;
  font-weight: 500;
}

.info-value {
  flex: 1;
  color: #606266;
}

.info-value.address {
  white-space: pre-line;
}

.product-list {
  border-radius: 8px;
  margin-bottom: 20px;
  background-color: rgba(255, 255, 255, 0.6);
  overflow: hidden;
  box-shadow: 0 1px 5px rgba(0, 0, 0, 0.05);
}

.product-item {
  display: flex;
  align-items: center;
  padding: 15px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  transition: background-color 0.3s;
}

.product-item:hover {
  background-color: rgba(255, 255, 255, 0.8);
}

.product-item:last-child {
  border-bottom: none;
}

.product-img {
  width: 80px;
  height: 80px;
  margin-right: 15px;
  flex-shrink: 0;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
}

.product-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-details {
  flex: 1;
  min-width: 0;
}

.product-title {
  margin: 0 0 5px 0;
  font-size: 16px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-weight: 600;
  color: #303133;
}

.product-desc {
  color: #909399;
  margin: 0;
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-price, .product-quantity, .product-subtotal {
  margin: 0 15px;
  white-space: nowrap;
}

.product-price {
  color: #f56c6c;
  width: 80px;
  text-align: center;
  flex-shrink: 0;
  font-weight: 500;
}

.product-quantity {
  width: 40px;
  text-align: center;
  flex-shrink: 0;
  color: #606266;
}

.product-subtotal {
  width: 80px;
  text-align: right;
  font-weight: bold;
  color: #f56c6c;
  flex-shrink: 0;
}

.product-actions {
  margin-left: 15px;
  flex-shrink: 0;
}

.review-btn {
  padding: 6px 10px;
  background-color: #e6a23c;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
}

.review-btn:hover {
  background-color: #d49731;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.order-total {
  background-color: rgba(255, 255, 255, 0.7);
  padding: 15px;
  border-radius: 8px;
  backdrop-filter: blur(10px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.total-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.total-row.amount {
  font-weight: bold;
  font-size: 18px;
  color: #f56c6c;
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px dashed #ddd;
}

.action-buttons {
  display: flex;
  justify-content: flex-end;
  gap: 15px;
  flex-wrap: wrap;
  margin-top: 30px;
}

.btn {
  padding: 10px 20px;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
  border: none;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}

.btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.15);
}

.pay-btn {
  background-color: #f56c6c;
  color: white;
}

.pay-btn:hover {
  background-color: #ec5454;
}

.cancel-btn {
  background-color: #909399;
  color: white;
}

.cancel-btn:hover {
  background-color: #82848a;
}

.confirm-btn {
  background-color: #67c23a;
  color: white;
}

.confirm-btn:hover {
  background-color: #5daf34;
}

.review-all-btn {
  background-color: #e6a23c;
  color: white;
}

.review-all-btn:hover {
  background-color: #d49731;
}

.contact-btn {
  background-color: #409EFF;
  color: white;
}

.contact-btn:hover {
  background-color: #3a8ee6;
}

.link-btn {
  padding: 10px 20px;
  background-color: #f4f4f5;
  color: #606266;
  text-decoration: none;
  border-radius: 4px;
  transition: all 0.3s;
  display: inline-block;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.05);
}

.link-btn:hover {
  color: #409EFF;
  background-color: #ecf5ff;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 0;
  background-color: rgba(255, 255, 255, 0.7);
  border-radius: 8px;
  backdrop-filter: blur(10px);
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid rgba(0, 0, 0, 0.1);
  border-top: 4px solid #409EFF;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 20px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.error-container {
  text-align: center;
  padding: 40px 0;
  color: #f56c6c;
  background-color: rgba(255, 255, 255, 0.7);
  border-radius: 8px;
  backdrop-filter: blur(10px);
}

.error-container i {
  font-size: 48px;
  margin-bottom: 20px;
}

.retry-btn {
  margin-top: 20px;
  padding: 8px 16px;
  background-color: #409EFF;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
  transition: all 0.3s;
}

.retry-btn:hover {
  background-color: #3a8ee6;
  transform: translateY(-2px);
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.15);
}

.empty-state {
  text-align: center;
  padding: 40px 0;
  color: #909399;
  background-color: rgba(255, 255, 255, 0.7);
  border-radius: 8px;
  backdrop-filter: blur(10px);
}

.empty-state i {
  font-size: 48px;
  margin-bottom: 20px;
}


</style>