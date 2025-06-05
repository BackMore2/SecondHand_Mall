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
      
      <div class="order-status-banner" :class="orderStatusClass">
        <div class="status-icon">
          <i :class="statusIcon"></i>
        </div>
        <div class="status-info">
          <h2>{{ orderInfo.status }}</h2>
          <p v-if="orderInfo.status === '待付款'">
            请在 <span class="countdown">{{ remainingTime }}</span> 内完成支付
          </p>
          <p v-else-if="orderInfo.status === '待发货'">
            卖家正在处理您的订单，请耐心等待
          </p>
          <p v-else-if="orderInfo.status === '已发货'">
            您的包裹已在路上，请注意查收
          </p>
          <p v-else-if="orderInfo.status === '已完成'">
            交易已完成，感谢您的购买
          </p>
          <p v-else-if="orderInfo.status === '已取消'">
            订单已取消
          </p>
        </div>
      </div>
      
      <!-- 订单基本信息 -->
      <div class="info-section order-base-info">
        <h3>订单信息</h3>
        <div class="info-row">
          <span class="info-label">订单编号：</span>
          <span class="info-value">{{ orderInfo.id }}</span>
        </div>
        <div class="info-row">
          <span class="info-label">下单时间：</span>
          <span class="info-value">{{ formatTime(orderInfo.createTime) }}</span>
        </div>
        <div class="info-row">
          <span class="info-label">支付方式：</span>
          <span class="info-value">{{ paymentMethod }}</span>
        </div>
        <div class="info-row">
          <span class="info-label">支付时间：</span>
          <span class="info-value">{{ orderInfo.payTime ? formatTime(orderInfo.payTime) : '未支付' }}</span>
        </div>
      </div>
      
      <!-- 收货信息 -->
      <div class="info-section delivery-info">
        <h3>收货信息</h3>
        <div class="info-row">
          <span class="info-label">收货人：</span>
          <span class="info-value">{{ orderInfo.recipient }}</span>
        </div>
        <div class="info-row">
          <span class="info-label">联系电话：</span>
          <span class="info-value">{{ orderInfo.phone }}</span>
        </div>
        <div class="info-row">
          <span class="info-label">收货地址：</span>
          <span class="info-value address">{{ orderInfo.address }}</span>
        </div>
      </div>
      
      <!-- 商品信息 -->
      <div class="info-section product-info">
        <h3>商品信息</h3>
        <div class="product-list">
          <div v-for="(item, index) in orderInfo.products" :key="index" class="product-item">
            <div class="product-img">
              <img :src="item.image" :alt="item.title" />
            </div>
            <div class="product-details">
              <h4 class="product-title">{{ item.title }}</h4>
              <p class="product-desc">{{ item.description }}</p>
            </div>
            <div class="product-price">¥{{ item.price.toFixed(2) }}</div>
            <div class="product-quantity">x{{ item.quantity }}</div>
            <div class="product-subtotal">¥{{ (item.price * item.quantity).toFixed(2) }}</div>
          </div>
        </div>
        <div class="order-total">
          <div class="total-row">
            <span>商品总计：</span>
            <span>¥{{ calculateSubtotal().toFixed(2) }}</span>
          </div>
          <div class="total-row">
            <span>运费：</span>
            <span>¥{{ orderInfo.shipping.toFixed(2) }}</span>
          </div>
          <div class="total-row amount">
            <span>订单总计：</span>
            <span>¥{{ (calculateSubtotal() + orderInfo.shipping).toFixed(2) }}</span>
          </div>
        </div>
      </div>
      
      <!-- 物流信息 -->
      <div class="info-section logistics-info" v-if="orderInfo.status === '已发货' || orderInfo.status === '已完成'">
        <h3>物流信息</h3>
        <div class="logistics-details">
          <div class="info-row">
            <span class="info-label">物流公司：</span>
            <span class="info-value">{{ orderInfo.logistics.company }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">物流单号：</span>
            <span class="info-value">{{ orderInfo.logistics.trackingNumber }}</span>
          </div>
        </div>
        <div class="tracking-timeline">
          <div v-for="(track, index) in orderInfo.logistics.tracking" :key="index" class="tracking-item">
            <div class="tracking-time">{{ formatTime(track.time) }}</div>
            <div class="tracking-info">{{ track.info }}</div>
          </div>
        </div>
      </div>
      
      <!-- 操作按钮 -->
      <div class="action-buttons">
        <button v-if="orderInfo.status === '待付款'" class="btn pay-btn" @click="goToPay">
          去支付
        </button>
        <button v-if="orderInfo.status === '待付款'" class="btn cancel-btn" @click="cancelOrder">
          取消订单
        </button>
        <button v-if="orderInfo.status === '已发货'" class="btn confirm-btn" @click="confirmReceive">
          确认收货
        </button>
        <button v-if="orderInfo.status === '已完成'" class="btn review-btn" @click="reviewOrder">
          评价订单
        </button>
        <button class="btn contact-btn" @click="contactSeller">
          联系卖家
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import NavBar from '../components/NavBar.vue'

const route = useRoute()
const router = useRouter()
const orderId = route.params.id

// 模拟订单数据
const orderInfo = ref({
  id: 'ORD20230515001',
  status: '已发货',
  createTime: new Date('2023-05-15T10:30:00'),
  payTime: new Date('2023-05-15T10:35:00'),
  paymentMethodId: 'alipay',
  recipient: '张三',
  phone: '13800138000',
  address: '北京市海淀区清华大学29号楼',
  shipping: 10,
  products: [
    {
      id: 1,
      title: '二手笔记本电脑',
      description: 'i5处理器，8GB内存，256GB固态硬盘',
      price: 2899,
      quantity: 1,
      image: 'https://via.placeholder.com/100'
    },
    {
      id: 2,
      title: '专业相机套装',
      description: '含18-55mm镜头，64GB存储卡',
      price: 1299,
      quantity: 1,
      image: 'https://via.placeholder.com/100'
    }
  ],
  logistics: {
    company: '顺丰速运',
    trackingNumber: 'SF1234567890',
    tracking: [
      { time: new Date('2023-05-16T14:00:00'), info: '快件已由【北京海淀区中转站】发出' },
      { time: new Date('2023-05-16T10:30:00'), info: '快件已到达【北京海淀区中转站】' },
      { time: new Date('2023-05-15T18:45:00'), info: '卖家已发货' }
    ]
  }
})

// 支付方式映射
const paymentMethods = {
  alipay: '支付宝',
  wechat: '微信支付',
  card: '银行卡'
}

// 获取支付方式名称
const paymentMethod = computed(() => {
  return paymentMethods[orderInfo.value.paymentMethodId] || '未知'
})

// 计算订单状态对应的样式类
const orderStatusClass = computed(() => {
  const status = orderInfo.value.status
  const classMap = {
    '待付款': 'status-pending-payment',
    '待发货': 'status-pending-shipment',
    '已发货': 'status-shipped',
    '已完成': 'status-completed',
    '已取消': 'status-canceled'
  }
  return classMap[status] || ''
})

// 计算订单状态对应的图标
const statusIcon = computed(() => {
  const status = orderInfo.value.status
  const iconMap = {
    '待付款': 'fas fa-credit-card',
    '待发货': 'fas fa-box',
    '已发货': 'fas fa-shipping-fast',
    '已完成': 'fas fa-check-circle',
    '已取消': 'fas fa-times-circle'
  }
  return iconMap[status] || 'fas fa-question-circle'
})

// 计算剩余支付时间（假设有30分钟支付时间）
const remainingTime = ref('00:00:00')

// 格式化时间
function formatTime(time) {
  if (!time) return ''
  return new Date(time).toLocaleString()
}

// 计算商品总价
function calculateSubtotal() {
  return orderInfo.value.products.reduce((sum, item) => sum + item.price * item.quantity, 0)
}

// 前往支付页面
function goToPay() {
  router.push(`/order/pay/${orderInfo.value.id}`)
}

// 取消订单
function cancelOrder() {
  if (confirm('确定要取消此订单吗？')) {
    // 这里应该调用API取消订单
    orderInfo.value.status = '已取消'
  }
}

// 确认收货
function confirmReceive() {
  if (confirm('确认已收到商品？')) {
    // 这里应该调用API确认收货
    orderInfo.value.status = '已完成'
  }
}

// 评价订单
function reviewOrder() {
  router.push(`/order/review/${orderInfo.value.id}`)
}

// 联系卖家
function contactSeller() {
  router.push(`/messages?seller=${orderInfo.value.id}`)
}

// 模拟获取订单数据
onMounted(() => {
  // 这里应该调用API获取订单数据
  console.log('获取订单详情，ID:', orderId)
  
  // 如果状态是待付款，开始倒计时
  if (orderInfo.value.status === '待付款') {
    // 计算剩余时间
    const deadline = new Date(orderInfo.value.createTime)
    deadline.setMinutes(deadline.getMinutes() + 30)
    
    const timer = setInterval(() => {
      const now = new Date()
      const diff = deadline - now
      
      if (diff <= 0) {
        clearInterval(timer)
        remainingTime.value = '00:00:00'
        // 订单超时，自动取消
        orderInfo.value.status = '已取消'
        return
      }
      
      // 格式化剩余时间
      const hours = Math.floor(diff / 3600000).toString().padStart(2, '0')
      const minutes = Math.floor((diff % 3600000) / 60000).toString().padStart(2, '0')
      const seconds = Math.floor((diff % 60000) / 1000).toString().padStart(2, '0')
      remainingTime.value = `${hours}:${minutes}:${seconds}`
    }, 1000)
  }
})
</script>

<style scoped>
.order-detail-page {
  min-height: 100vh;
}

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
  max-width: 1000px;
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

.countdown {
  font-weight: bold;
}

.info-section {
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #eee;
}

.info-section h3 {
  margin-bottom: 15px;
  font-size: 18px;
  color: #555;
}

.info-row {
  display: flex;
  margin-bottom: 10px;
}

.info-label {
  width: 100px;
  color: #909399;
}

.info-value {
  flex: 1;
  color: #606266;
}

.info-value.address {
  white-space: pre-line;
}

.product-list {
  border: 1px solid #eee;
  border-radius: 4px;
  margin-bottom: 20px;
}

.product-item {
  display: flex;
  align-items: center;
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

.product-details {
  flex: 1;
}

.product-title {
  margin: 0 0 5px 0;
  font-size: 16px;
}

.product-desc {
  color: #909399;
  margin: 0;
  font-size: 14px;
}

.product-price, .product-quantity, .product-subtotal {
  margin: 0 15px;
}

.product-price {
  color: #f56c6c;
  width: 100px;
  text-align: center;
}

.product-quantity {
  width: 50px;
  text-align: center;
}

.product-subtotal {
  width: 100px;
  text-align: right;
  font-weight: bold;
  color: #f56c6c;
}

.order-total {
  background-color: #f9f9f9;
  padding: 15px;
  border-radius: 4px;
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

.logistics-details {
  margin-bottom: 20px;
}

.tracking-timeline {
  border-left: 2px solid #eee;
  padding-left: 20px;
  margin-left: 10px;
}

.tracking-item {
  position: relative;
  margin-bottom: 15px;
}

.tracking-item:before {
  content: '';
  position: absolute;
  left: -26px;
  top: 5px;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background-color: #409EFF;
}

.tracking-item:first-child:before {
  background-color: #67c23a;
}

.tracking-time {
  color: #909399;
  font-size: 14px;
  margin-bottom: 5px;
}

.tracking-info {
  color: #606266;
}

.action-buttons {
  display: flex;
  justify-content: flex-end;
  gap: 15px;
}

.btn {
  padding: 10px 20px;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
  border: none;
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

.review-btn {
  background-color: #e6a23c;
  color: white;
}

.review-btn:hover {
  background-color: #d49731;
}

.contact-btn {
  background-color: #409EFF;
  color: white;
}

.contact-btn:hover {
  background-color: #3a8ee6;
}
</style> 