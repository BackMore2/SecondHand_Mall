<template>
    <div class="order-complete-page">
      <!-- 导航栏 -->
      <NavBar />
      
      <div class="container">
        <div class="complete-content">
          <!-- 成功图标 -->
          <div class="success-icon">
            <i class="fas fa-check-circle"></i>
          </div>
          
          <!-- 成功信息 -->
          <h2 class="success-title">支付成功</h2>
          <p class="success-message">您的订单已支付完成，感谢您的购买！</p>
          
          <!-- 订单信息 -->
          <div class="order-info" v-if="order">
            <div class="info-item">
              <span class="label">订单编号:</span>
              <span class="value">{{ order.orderNumber || order.id }}</span>
            </div>
            <div class="info-item">
              <span class="label">支付金额:</span>
              <span class="value price">¥{{ order.totalPrice.toFixed(2) }}</span>
            </div>
            <div class="info-item">
              <span class="label">支付方式:</span>
              <span class="value">{{ getPaymentMethodName(order.paymentMethod) }}</span>
            </div>
            <div class="info-item">
              <span class="label">支付时间:</span>
              <span class="value">{{ formatTime(new Date()) }}</span>
            </div>
          </div>
          
          <!-- 按钮区域 -->
          <div class="action-buttons">
            <router-link :to="`/orders/${orderId}`" class="btn view-order-btn">
              查看订单详情
            </router-link>
            <router-link to="/orders" class="btn orders-btn">
              我的订单列表
            </router-link>
            <router-link to="/" class="btn home-btn">
              返回首页
            </router-link>
          </div>
        </div>
      </div>
    </div>
  </template>
  
  <script setup>
  import { ref, onMounted } from 'vue'
  import { useRoute, useRouter } from 'vue-router'
  import { useOrderStore } from '@/stores/order'
  import NavBar from '@/components/NavBar.vue'
  
  const route = useRoute()
  const router = useRouter()
  const orderStore = useOrderStore()
  
  const orderId = Number(route.params.id)
  const order = ref(null)
  
  // 支付方式映射
  const paymentMethods = {
    'alipay': '支付宝',
    'wechat': '微信支付',
    'card': '银行卡支付'
  }
  
  // 获取支付方式显示名称
  const getPaymentMethodName = (methodId) => {
    return paymentMethods[methodId] || methodId
  }
  
  // 格式化时间
  const formatTime = (time) => {
    if (!time) return '--'
    
    const date = new Date(time)
    return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
  }
  
  // 页面加载时获取订单信息
  onMounted(() => {
    if (!orderId) {
      router.push('/orders')
      return
    }
    
    // 从订单仓库获取订单信息
    order.value = orderStore.getOrderById(orderId)
    
    // 如果订单不存在，跳转到订单列表
    if (!order.value) {
      router.push('/orders')
    }
    
    // 更新订单状态为已支付
    if (order.value) {
      orderStore.payOrder(orderId)
    }
  })
  </script>
  
  <style scoped>
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
    max-width: 800px;
  }
  
  .complete-content {
    display: flex;
    flex-direction: column;
    align-items: center;
    text-align: center;
  }
  
  .success-icon {
    font-size: 80px;
    color: #67C23A;
    margin-bottom: 20px;
  }
  
  .success-title {
    font-size: 24px;
    color: #303133;
    margin-bottom: 10px;
  }
  
  .success-message {
    font-size: 16px;
    color: #606266;
    margin-bottom: 30px;
  }
  
  .order-info {
    width: 100%;
    max-width: 500px;
    background-color: #f8f8f8;
    border-radius: 8px;
    padding: 20px;
    margin-bottom: 30px;
  }
  
  .info-item {
    display: flex;
    justify-content: space-between;
    margin-bottom: 10px;
    padding: 5px 0;
  }
  
  .info-item:last-child {
    margin-bottom: 0;
  }
  
  .label {
    color: #909399;
    flex-shrink: 0;
  }
  
  .value {
    color: #606266;
    font-weight: 500;
    text-align: right;
  }
  
  .value.price {
    color: #F56C6C;
    font-weight: 600;
  }
  
  .action-buttons {
    display: flex;
    flex-wrap: wrap;
    justify-content: center;
    gap: 15px;
    margin-top: 20px;
  }
  
  .btn {
    padding: 10px 20px;
    border-radius: 4px;
    font-size: 14px;
    cursor: pointer;
    text-decoration: none;
    transition: all 0.3s;
  }
  
  .view-order-btn {
    background-color: #409EFF;
    color: white;
  }
  
  .view-order-btn:hover {
    background-color: #66b1ff;
  }
  
  .orders-btn {
    background-color: #67C23A;
    color: white;
  }
  
  .orders-btn:hover {
    background-color: #85ce61;
  }
  
  .home-btn {
    background-color: #f4f4f5;
    color: #606266;
    border: 1px solid #dcdfe6;
  }
  
  .home-btn:hover {
    color: #409EFF;
    border-color: #c6e2ff;
    background-color: #ecf5ff;
  }
  </style>