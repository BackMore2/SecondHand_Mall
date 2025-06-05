<template>
  <div class="order-create-page">
    <!-- 添加导航栏 -->
    <NavBar />
    
    <div class="content-container">
      <h2>创建订单</h2>
      <div class="order-form">
        <!-- 收货信息 -->
        <div class="section">
          <h3>收货信息</h3>
          <div class="form-group">
            <label>收货人</label>
            <input type="text" v-model="orderInfo.name" placeholder="请输入收货人姓名" />
          </div>
          <div class="form-group">
            <label>手机号</label>
            <input type="text" v-model="orderInfo.phone" placeholder="请输入联系电话" />
          </div>
          <div class="form-group">
            <label>收货地址</label>
            <textarea v-model="orderInfo.address" placeholder="请输入详细地址"></textarea>
          </div>
        </div>
        
        <!-- 商品信息 -->
        <div class="section">
          <h3>商品信息</h3>
          <div class="product-list">
            <div v-for="(item, index) in products" :key="index" class="product-item">
              <div class="product-img">
                <img :src="item.image" :alt="item.name" />
              </div>
              <div class="product-info">
                <h4>{{ item.name }}</h4>
                <p class="price">¥{{ item.price.toFixed(2) }}</p>
                <p class="quantity">x{{ item.quantity }}</p>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 支付方式 -->
        <div class="section">
          <h3>支付方式</h3>
          <div class="payment-methods">
            <div 
              v-for="method in paymentMethods" 
              :key="method.id"
              class="payment-method"
              :class="{ active: orderInfo.paymentMethod === method.id }"
              @click="orderInfo.paymentMethod = method.id"
            >
              <i :class="method.icon"></i>
              <span>{{ method.name }}</span>
            </div>
          </div>
        </div>
        
        <!-- 订单摘要 -->
        <div class="section order-summary">
          <div class="summary-row">
            <span>商品金额：</span>
            <span>¥{{ totalPrice.toFixed(2) }}</span>
          </div>
          <div class="summary-row">
            <span>运费：</span>
            <span>¥{{ shipping.toFixed(2) }}</span>
          </div>
          <div class="summary-row total">
            <span>订单总计：</span>
            <span>¥{{ (totalPrice + shipping).toFixed(2) }}</span>
          </div>
        </div>
        
        <!-- 提交订单 -->
        <div class="submit-order">
          <button class="submit-btn" @click="submitOrder">提交订单</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useOrderStore } from '@/stores/order'
import { useCartStore } from '@/stores/cart'
import NavBar from '@/components/NavBar.vue'

// 路由器
const router = useRouter()

// 获取订单和购物车的store
const orderStore = useOrderStore()
const cartStore = useCartStore()

// 订单信息
const orderInfo = ref({
  name: '',
  phone: '',
  address: '',
  paymentMethod: 'alipay'
})

// 支付方式
const paymentMethods = [
  { id: 'alipay', name: '支付宝', icon: 'fab fa-alipay' },
  { id: 'wechat', name: '微信支付', icon: 'fab fa-weixin' },
  { id: 'card', name: '银行卡', icon: 'far fa-credit-card' }
]

// 获取要结算的商品
const products = ref([])

// 在页面加载时从sessionStorage获取结算商品信息
onMounted(() => {
  try {
    const checkoutItems = sessionStorage.getItem('checkoutItems')
    if (checkoutItems) {
      products.value = JSON.parse(checkoutItems)
    } else {
      // 如果没有结算商品，返回购物车页
      router.push('/cart')
    }
  } catch (error) {
    console.error('加载结算商品信息失败:', error)
    router.push('/cart')
  }
})

// 计算总价
const totalPrice = computed(() => {
  return products.value.reduce((sum, item) => sum + item.price * item.quantity, 0)
})

// 运费
const shipping = ref(10)

// 提交订单
function submitOrder() {
  // 验证表单
  if (!orderInfo.value.name || !orderInfo.value.phone || !orderInfo.value.address) {
    alert('请填写完整的收货信息')
    return
  }
  
  // 创建订单对象
  const order = {
    items: products.value,
    shipping: shipping.value,
    totalAmount: totalPrice.value + shipping.value,
    recipient: {
      name: orderInfo.value.name,
      phone: orderInfo.value.phone,
      address: orderInfo.value.address
    },
    paymentMethod: orderInfo.value.paymentMethod
  }
  
  try {
    // 调用store创建订单
    const orderId = orderStore.createOrder(order)
    
    // 清除购物车中已下单的商品
    const itemIds = products.value.map(item => item.id)
    itemIds.forEach(id => cartStore.removeFromCart(id))
    
    // 清除sessionStorage中的结算商品
    sessionStorage.removeItem('checkoutItems')
    
    // 跳转到支付页面
    router.push(`/order/pay/${orderId}`)
  } catch (error) {
    console.error('创建订单失败:', error)
    alert('创建订单失败，请重试')
  }
}
</script>

<style scoped>
.content-container {
  position: relative;
  z-index: 1;
  background-color: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border-radius: 20px;
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.2);
  margin: 2rem auto;
  padding: 2rem;
  max-width: 1000px;
}

.section {
  margin-bottom: 30px;
  border-bottom: 1px solid #eee;
  padding-bottom: 20px;
}

.section:last-child {
  border-bottom: none;
}

h2 {
  margin-bottom: 20px;
  color: #333;
}

h3 {
  margin-bottom: 15px;
  font-size: 18px;
  color: #555;
}

.form-group {
  margin-bottom: 15px;
}

label {
  display: block;
  margin-bottom: 5px;
  font-weight: 500;
}

input, textarea {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

textarea {
  height: 80px;
  resize: vertical;
}

.product-list {
  border: 1px solid #eee;
  border-radius: 4px;
}

.product-item {
  display: flex;
  padding: 15px;
  border-bottom: 1px solid #eee;
}

.product-item:last-child {
  border-bottom: none;
}

.product-img {
  width: 80px;
  height: 80px;
  margin-right: 15px;
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

.product-info h4 {
  margin: 0 0 5px 0;
  font-size: 16px;
}

.price {
  color: #f56c6c;
  font-weight: 500;
  margin: 5px 0;
}

.quantity {
  color: #999;
}

.payment-methods {
  display: flex;
  gap: 15px;
}

.payment-method {
  padding: 12px 20px;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.3s;
}

.payment-method.active {
  border-color: #409EFF;
  background-color: #ecf5ff;
  color: #409EFF;
}

.order-summary {
  background-color: #f9f9f9;
  padding: 15px;
  border-radius: 4px;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.summary-row.total {
  font-weight: bold;
  font-size: 18px;
  color: #f56c6c;
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px dashed #ddd;
}

.submit-order {
  text-align: center;
  margin-top: 30px;
}

.submit-btn {
  background-color: #f56c6c;
  color: white;
  border: none;
  padding: 12px 30px;
  font-size: 16px;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.submit-btn:hover {
  background-color: #ec5454;
}
</style> 