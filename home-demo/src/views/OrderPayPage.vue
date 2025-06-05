<template>
  <div class="order-pay-page">
    <!-- 导航栏 -->
    <NavBar />
    
    <div class="container" v-if="order">
      <div class="pay-header">
        <h2>订单支付</h2>
      </div>
      
      <div class="pay-content">
        <!-- 订单信息 -->
        <div class="order-info">
          <div class="order-number">订单编号: #{{ orderId }}</div>
          <div class="order-amount">支付金额: <span class="price">¥{{ order.totalAmount.toFixed(2) }}</span></div>
        </div>
        
        <!-- 支付方式 -->
        <div class="payment-methods">
          <h3>选择支付方式</h3>
          <div class="payment-options">
            <div 
              v-for="method in paymentMethods" 
              :key="method.id"
              class="payment-option"
              :class="{ active: selectedPayment === method.id }"
              @click="selectedPayment = method.id"
            >
              <i :class="method.icon"></i>
              <span>{{ method.name }}</span>
              <i class="fas fa-check-circle check-icon" v-if="selectedPayment === method.id"></i>
            </div>
          </div>
        </div>
        
        <!-- 支付按钮 -->
        <div class="payment-actions">
          <button class="pay-btn" @click="handlePay">立即支付</button>
          <button class="cancel-btn" @click="cancelPayment">取消</button>
        </div>
      </div>
    </div>
    
    <div class="container" v-else>
      <div class="error-message">
        订单信息不存在或已过期
      </div>
      <div class="actions">
        <router-link to="/orders" class="link-btn">查看我的订单</router-link>
        <router-link to="/" class="link-btn">返回首页</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useOrderStore } from '@/stores/order'
import NavBar from '@/components/NavBar.vue'

const route = useRoute()
const router = useRouter()
const orderStore = useOrderStore()

const orderId = Number(route.params.id)
const order = ref(null)
const selectedPayment = ref('')

// 支付方式选项
const paymentMethods = [
  { id: 'alipay', name: '支付宝', icon: 'fab fa-alipay' },
  { id: 'wechat', name: '微信支付', icon: 'fab fa-weixin' },
  { id: 'card', name: '银行卡支付', icon: 'far fa-credit-card' }
]

// 页面加载时获取订单信息
onMounted(() => {
  if (!orderId) {
    return
  }
  
  // 从orderStore获取订单信息
  order.value = orderStore.getOrderById(orderId)
  
  // 如果订单存在且有支付方式，则设为默认支付方式
  if (order.value && order.value.paymentMethod) {
    selectedPayment.value = order.value.paymentMethod
  } else if (paymentMethods.length > 0) {
    // 否则选择第一个支付方式
    selectedPayment.value = paymentMethods[0].id
  }
})

// 处理支付
const handlePay = () => {
  if (!order.value) {
    alert('订单信息不存在')
    return
  }
  
  if (!selectedPayment.value) {
    alert('请选择支付方式')
    return
  }
  
  try {
    // 模拟支付过程
    setTimeout(() => {
      // 调用orderStore的payOrder方法更新订单状态
      orderStore.payOrder(orderId)
      
      // 显示支付成功提示
      alert('支付成功!')
      
      // 跳转到订单详情页面
      router.push(`/orders/${orderId}`)
    }, 1000)
  } catch (error) {
    console.error('支付失败:', error)
    alert('支付失败，请重试')
  }
}

// 取消支付
const cancelPayment = () => {
  const confirmed = confirm('确定要取消支付吗？')
  if (confirmed) {
    router.push('/orders')
  }
}
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

.pay-header {
  text-align: center;
  padding-bottom: 20px;
  border-bottom: 1px solid #ebeef5;
  margin-bottom: 30px;
}

.pay-header h2 {
  margin: 0;
  color: #303133;
  font-size: 24px;
}

.order-info {
  text-align: center;
  margin-bottom: 30px;
  padding: 20px;
  background-color: #f8f8f8;
  border-radius: 6px;
}

.order-number {
  font-size: 14px;
  color: #606266;
  margin-bottom: 10px;
}

.order-amount {
  font-size: 16px;
  color: #303133;
}

.price {
  font-size: 24px;
  color: #f56c6c;
  font-weight: 600;
}

.payment-methods {
  margin-bottom: 30px;
}

.payment-methods h3 {
  font-size: 18px;
  color: #303133;
  margin-bottom: 15px;
}

.payment-options {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.payment-option {
  display: flex;
  align-items: center;
  padding: 15px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
  position: relative;
}

.payment-option i {
  font-size: 20px;
  margin-right: 10px;
}

.payment-option .check-icon {
  position: absolute;
  right: 15px;
  color: #67c23a;
  display: none;
}

.payment-option.active {
  border-color: #409EFF;
  background-color: #ecf5ff;
}

.payment-option.active .check-icon {
  display: block;
}

.payment-actions {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 30px;
}

.pay-btn {
  padding: 12px 30px;
  background-color: #409EFF;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s;
}

.pay-btn:hover {
  background-color: #66b1ff;
}

.cancel-btn {
  padding: 12px 30px;
  background-color: #f4f4f5;
  color: #909399;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s;
}

.cancel-btn:hover {
  color: #409EFF;
  border-color: #c6e2ff;
  background-color: #ecf5ff;
}

.error-message {
  text-align: center;
  font-size: 18px;
  color: #f56c6c;
  margin-bottom: 30px;
}

.actions {
  display: flex;
  justify-content: center;
  gap: 20px;
}

.link-btn {
  padding: 10px 20px;
  background-color: #f4f4f5;
  color: #606266;
  text-decoration: none;
  border-radius: 4px;
  transition: all 0.3s;
}

.link-btn:hover {
  color: #409EFF;
  background-color: #ecf5ff;
}
</style> 