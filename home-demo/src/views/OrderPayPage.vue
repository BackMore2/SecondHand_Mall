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
          <div class="order-number">订单编号: <span class="order-id">#{{ order.orderNumber || order.id }}</span></div>
          <div class="order-amount">支付金额: <span class="price">¥{{ order.totalPrice.toFixed(2) }}</span></div>
          
          <!-- 订单详细信息 -->
          <div class="order-details">
            <div class="info-section">
              <h3>收货信息</h3>
              <div class="info-row">
                <span class="info-label">收货人:</span>
                <span class="info-value">{{ order.recipientName }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">联系电话:</span>
                <span class="info-value">{{ order.recipientPhone }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">收货地址:</span>
                <span class="info-value">{{ order.shippingAddress }}</span>
              </div>
            </div>
            
            <div class="info-section">
              <h3>商品信息</h3>
              <div class="product-list">
                <div v-for="(item, index) in order.items" :key="index" class="product-item">
                  <div class="product-img" v-if="item.image">
                    <img :src="item.image" :alt="item.name || '商品图片'" />
                  </div>
                  <div class="product-info">
                    <div class="product-name">{{ item.name || `商品 #${item.productId}` }}</div>
                    <div class="product-price">¥{{ item.price.toFixed(2) }} × {{ item.quantity }}</div>
                  </div>
                  <div class="product-total">¥{{ (item.price * item.quantity).toFixed(2) }}</div>
                </div>
              </div>
            </div>
          </div>
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
        
        <!-- 支付按钮区域 -->
        <div class="payment-actions">
          <!-- 前去支付按钮 -->
          <button class="pay-btn" @click="handlePay">
            <i class="fas fa-credit-card"></i> 前去支付
          </button>
          
          <!-- 我已完成支付按钮 -->
          <button class="completed-btn" @click="completePayment">
            <i class="fas fa-check"></i> 我已完成支付
          </button>
          
          <!-- 取消按钮 -->
          <button class="cancel-btn" @click="cancelPayment">
            <i class="fas fa-times"></i> 取消
          </button>
        </div>
        
        <!-- 支付说明 -->
        <div class="payment-notes">
          <p><i class="fas fa-info-circle"></i> 请在新打开的页面完成支付，支付完成后请点击"我已完成支付"按钮</p>
          <p><i class="fas fa-exclamation-triangle"></i> 请在30分钟内完成支付，否则订单将自动取消</p>
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
import * as orderApi from '@/api/order'
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
onMounted(async () => {
  if (!orderId) {
    console.error('未提供订单ID');
    return;
  }
  
  console.log('正在加载订单信息，ID:', orderId);
  
  // 首先尝试从sessionStorage获取
  const savedOrder = sessionStorage.getItem('currentOrder');
  if (savedOrder) {
    try {
      const parsedOrder = JSON.parse(savedOrder);
      // 确认是当前需要的订单
      if (parsedOrder && parsedOrder.id === orderId) {
        console.log('从sessionStorage加载到订单信息:', parsedOrder);
        order.value = parsedOrder;
        
        // 如果订单存在且有支付方式，则设为默认支付方式
        if (parsedOrder.paymentMethod) {
          selectedPayment.value = parsedOrder.paymentMethod;
        }
      }
    } catch (error) {
      console.error('解析sessionStorage中的订单信息失败:', error);
    }
  }
  
  // 如果从sessionStorage未获取到订单信息，尝试从store获取
  if (!order.value) {
    console.log('从orderStore获取订单信息');
    const storeOrder = orderStore.getOrderById(orderId);
    if (storeOrder) {
      // 创建一个干净的订单对象，避免任何嵌套
      order.value = {
        id: storeOrder.id,
        orderNumber: storeOrder.orderNumber,
        userId: storeOrder.userId,
        totalPrice: storeOrder.totalPrice,
        status: storeOrder.status,
        shippingAddress: storeOrder.shippingAddress,
        paymentMethod: storeOrder.paymentMethod,
        recipientName: storeOrder.recipientName,
        recipientPhone: storeOrder.recipientPhone,
        createTime: storeOrder.createTime,
        items: []
      };
      
      // 如果store中的订单有items，复制它们
      if (storeOrder.items && Array.isArray(storeOrder.items)) {
        order.value.items = storeOrder.items.map(item => ({
          id: item.id,
          productId: item.productId,
          quantity: item.quantity,
          price: item.price,
          totalPrice: typeof item.totalPrice === 'number' ? item.totalPrice : (item.price * item.quantity),
          name: item.name || `商品 #${item.productId}`,
          image: item.image || null
        }));
      }
      
      console.log('从store加载到订单信息:', order.value);
  
  // 如果订单存在且有支付方式，则设为默认支付方式
      if (storeOrder.paymentMethod) {
        selectedPayment.value = storeOrder.paymentMethod;
      }
    }
  }
  
  // 如果仍然没有订单信息，尝试从API获取
  if (!order.value) {
    try {
      console.log('尝试从API获取订单信息');
      const apiOrder = await orderApi.getOrderDetail(orderId);
      if (apiOrder) {
        // 创建一个干净的订单对象
        order.value = {
          id: apiOrder.id,
          orderNumber: apiOrder.orderNumber,
          userId: apiOrder.userId,
          totalPrice: apiOrder.totalPrice,
          status: apiOrder.status,
          shippingAddress: apiOrder.shippingAddress,
          paymentMethod: apiOrder.paymentMethod,
          recipientName: apiOrder.recipientName,
          recipientPhone: apiOrder.recipientPhone,
          createTime: apiOrder.createdAt || apiOrder.createTime,
          items: []
        };
        
        // 单独获取订单项
        try {
          const orderItems = await orderApi.getOrderItems(orderId);
          if (orderItems && Array.isArray(orderItems)) {
            order.value.items = orderItems.map(item => ({
              id: item.id,
              productId: item.productId,
              quantity: item.quantity,
              price: item.price,
              totalPrice: item.price * item.quantity,
              name: `商品 #${item.productId}`,
              image: null
            }));
          }
        } catch (itemError) {
          console.error('获取订单项失败:', itemError);
        }
        
        console.log('从API加载到订单信息:', order.value);
        
        // 保存到sessionStorage以便下次使用
        sessionStorage.setItem('currentOrder', JSON.stringify(order.value));
      }
    } catch (error) {
      console.error('从API获取订单信息失败:', error);
    }
  }
  
  // 如果没有设置支付方式，则使用默认的第一个
  if (!selectedPayment.value && paymentMethods.length > 0) {
    selectedPayment.value = paymentMethods[0].id;
  }
  
  // 如果仍然没有订单信息，显示错误
  if (!order.value) {
    console.error('无法加载订单信息，订单ID:', orderId);
  }
})

// 前去支付
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
    // 在这里可以调用真实的支付接口
    // 例如打开支付宝或微信支付页面
    
    // 这里仅作示例，实际应该根据选择的支付方式打开对应的支付界面
    if (selectedPayment.value === 'alipay') {
      alert('即将跳转到支付宝支付页面')
      // window.open('支付宝支付URL')
    } else if (selectedPayment.value === 'wechat') {
      alert('请使用微信扫描二维码完成支付')
      // 显示微信支付二维码
    } else if (selectedPayment.value === 'card') {
      alert('即将跳转到银行卡支付页面')
      // window.open('银行支付URL')
    }
  } catch (error) {
    console.error('跳转支付页面失败:', error)
    alert('跳转支付页面失败，请重试')
  }
}

// 我已完成支付
const completePayment = async () => {
  if (!order.value) {
    alert('订单信息不存在')
    return
  }
  
  try {
    // 更新订单状态直接为已完成，而不是待发货
    // 这里先调用payOrder把状态改为待发货
      orderStore.payOrder(orderId)
      
    // 然后再调用confirmOrder把状态改为已完成
    orderStore.confirmOrder(orderId)
    
    // 也可以考虑调用API更新状态
    try {
      // 先支付
      await orderApi.payOrder(orderId, { paymentMethod: selectedPayment.value })
      console.log('订单支付状态已更新')
      
      // 然后确认收货（跳过发货环节，直接完成）
      await orderApi.confirmOrder(orderId)
      console.log('订单已标记为已完成')
    } catch (apiError) {
      console.error('API更新订单状态失败，但本地状态已更新:', apiError)
    }
    
    // 显示成功消息
    alert('支付成功，订单已完成！')
      
    // 跳转到订单完成页面
    router.push(`/order/complete/${orderId}`)
  } catch (error) {
    console.error('确认支付失败:', error)
    alert('确认支付失败，请联系客服')
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

.order-id {
  font-weight: bold;
  color: #303133;
}

.order-amount {
  font-size: 16px;
  color: #303133;
  margin-bottom: 20px;
}

.price {
  font-size: 24px;
  color: #f56c6c;
  font-weight: 600;
}

/* 订单详细信息 */
.order-details {
  margin-top: 20px;
  text-align: left;
}

.info-section {
  margin-bottom: 20px;
}

.info-section h3 {
  font-size: 16px;
  color: #303133;
  margin-bottom: 10px;
  padding-bottom: 5px;
  border-bottom: 1px solid #ebeef5;
}

.info-row {
  display: flex;
  margin-bottom: 5px;
}

.info-label {
  color: #909399;
  min-width: 80px;
}

.info-value {
  color: #606266;
  flex: 1;
}

.product-list {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  overflow: hidden;
}

.product-item {
  display: flex;
  align-items: center;
  padding: 10px;
  border-bottom: 1px solid #ebeef5;
}

.product-item:last-child {
  border-bottom: none;
}

.product-img {
  width: 50px;
  height: 50px;
  margin-right: 10px;
}

.product-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 4px;
}

.product-info {
  flex: 1;
}

.product-name {
  font-size: 14px;
  color: #303133;
  margin-bottom: 5px;
}

.product-price {
  font-size: 12px;
  color: #909399;
}

.product-total {
  font-size: 14px;
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
  flex-wrap: wrap;
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

.completed-btn {
  padding: 12px 30px;
  background-color: #67C23A;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s;
}

.completed-btn:hover {
  background-color: #85ce61;
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

.payment-notes {
  margin-top: 30px;
  padding: 15px;
  background-color: #fdf6ec;
  border-radius: 4px;
  color: #e6a23c;
}

.payment-notes p {
  margin: 5px 0;
  font-size: 14px;
}

.payment-notes i {
  margin-right: 5px;
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