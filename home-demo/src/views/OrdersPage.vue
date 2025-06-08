<template>
  <div class="orders-page">
    <!-- 导航栏 -->
    <NavBar />
    
    <!-- 加载状态显示 -->
    <div v-if="loading" class="loading-container">
      <div class="loading-spinner">
        <i class="fas fa-spinner fa-spin"></i>
      </div>
      <p>正在加载订单数据...</p>
    </div>
    
    <!-- 内容容器 -->
    <div v-else class="container">
      <div class="section-header">
        <h2 class="section-title">我的订单</h2>
        <!-- 订单筛选标签 -->
        <div class="order-tabs">
          <span 
            v-for="tab in orderTabs" 
            :key="tab.value" 
            :class="['tab-item', { active: currentTab === tab.value }]"
            @click="currentTab = tab.value"
          >
            {{ tab.label }}
          </span>
        </div>
      </div>
      
      <!-- 空订单提示 -->
      <div v-if="filteredOrders.length === 0" class="empty-orders">
        <i class="fas fa-box-open"></i>
        <p>{{ emptyOrderMessage }}</p>
        <router-link to="/" class="go-shop">去逛逛</router-link>
      </div>
      
      <!-- 订单列表 -->
      <div v-else class="orders-list">
        <div v-for="order in filteredOrders" :key="order.id" class="order-card">
          <div class="order-header">
            <div class="order-info">
              <span class="order-time">下单时间: {{ formatTime(order.createTime) }}</span>
              <span class="order-number">订单号: {{ order.orderNumber || order.id }}</span>
            </div>
            <div class="order-status" :class="getStatusClass(order.status)">
              {{ getStatusText(order.status) }}
            </div>
          </div>
          
          <!-- 订单简要信息 -->
          <div class="order-brief">
            <div class="order-address">
              <i class="fas fa-map-marker-alt"></i>
              <span>{{ order.shippingAddress || '暂无地址信息' }}</span>
            </div>
            
            <div class="order-payment">
              <i class="fas fa-credit-card"></i>
              <span>{{ order.paymentMethod || '未选择支付方式' }}</span>
            </div>
          </div>
          
          <div class="order-footer">
            <div class="order-total">
              <span class="price">订单金额: <span class="amount">¥{{ order.totalPrice ? order.totalPrice.toFixed(2) : '0.00' }}</span></span>
            </div>
            
            <div class="order-actions">
              <!-- 已完成状态 - 只显示订单详情按钮 -->
              <template v-if="order.status === 'COMPLETED' || order.status === '已完成'">
                <router-link :to="`/orders/${order.id}`" class="btn detail-btn">
                  <i class="fas fa-list-ul"></i> 查看订单详情
                </router-link>
              </template>
              
              <!-- 其他状态 - 显示相应的操作按钮 -->
              <template v-else>
                <!-- 待付款状态 -->
                <template v-if="order.status === 'PENDING' || order.status === '待付款'">
                  <button class="btn pay-btn" @click="goToPay(order)">
                    <i class="fas fa-credit-card"></i> 去支付
                  </button>
                  <button class="btn cancel-btn" @click="cancelOrder(order.id)">
                    <i class="fas fa-times"></i> 取消订单
                  </button>
                </template>
                
                <!-- 待发货状态 -->
                <template v-else-if="order.status === 'PAID' || order.status === '待发货'">
                  <button class="btn contact-btn">
                    <i class="fas fa-comment"></i> 联系卖家
                  </button>
                </template>
                
                <!-- 已发货状态 -->
                <template v-else-if="order.status === 'SHIPPED' || order.status === '已发货'">
                  <button class="btn confirm-btn" @click="confirmReceive(order.id)">
                    <i class="fas fa-check"></i> 确认收货
                  </button>
                  <button class="btn track-btn">
                    <i class="fas fa-truck"></i> 查看物流
                  </button>
                </template>
                
                <!-- 已取消状态 -->
                <template v-else-if="order.status === 'CANCELED' || order.status === '已取消'">
                  <button class="btn delete-btn" @click="deleteOrder(order.id)">
                    <i class="fas fa-trash"></i> 删除订单
                  </button>
                </template>
                
                <!-- 订单详情按钮 -->
                <router-link :to="`/orders/${order.id}`" class="btn detail-btn">
                  <i class="fas fa-list-ul"></i> 订单详情
                </router-link>
              </template>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- Toast消息提示 -->
    <div v-if="toast.show" class="toast" :class="toast.type">
      {{ toast.message }}
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useOrderStore } from '@/stores/order'
import { useUserStore } from '@/stores/user'
import * as orderApi from '@/api/order'
import NavBar from '@/components/NavBar.vue'

const router = useRouter()
const orderStore = useOrderStore()
const userStore = useUserStore()

// 状态变量
const loading = ref(true)
const orders = ref([])
const currentTab = ref('all')
const toast = reactive({
  show: false,
  message: '',
  type: 'info',
  timer: null
})

// 订单标签选项
const orderTabs = [
  { label: '全部', value: 'all' },
  { label: '待付款', value: 'pending' },
  { label: '待发货', value: 'paid' },
  { label: '待收货', value: 'shipped' },
  { label: '已完成', value: 'completed' }
]

// 根据当前标签筛选订单
const filteredOrders = computed(() => {
  if (currentTab.value === 'all') {
    return orders.value
  }
  
  const statusMap = {
    'pending': ['PENDING', '待付款'],
    'paid': ['PAID', '待发货'],
    'shipped': ['SHIPPED', '已发货'],
    'completed': ['COMPLETED', '已完成']
  }
  
  const statuses = statusMap[currentTab.value] || []
  return orders.value.filter(order => statuses.includes(order.status))
})

// 空订单提示信息
const emptyOrderMessage = computed(() => {
  if (currentTab.value === 'all') {
    return '您还没有订单'
  } else if (currentTab.value === 'pending') {
    return '您没有待付款的订单'
  } else if (currentTab.value === 'paid') {
    return '您没有待发货的订单'
  } else if (currentTab.value === 'shipped') {
    return '您没有待收货的订单'
  } else if (currentTab.value === 'completed') {
    return '您没有已完成的订单'
  }
  return '没有找到符合条件的订单'
})

// 获取状态样式类名
const getStatusClass = (status) => {
  if (['PENDING', '待付款'].includes(status)) return 'status-pending'
  if (['PAID', '待发货'].includes(status)) return 'status-paid'
  if (['SHIPPED', '已发货'].includes(status)) return 'status-shipped'
  if (['COMPLETED', '已完成'].includes(status)) return 'status-completed'
  if (['CANCELED', '已取消'].includes(status)) return 'status-canceled'
  return ''
}

// 获取状态显示文本
const getStatusText = (status) => {
  const statusMap = {
    'PENDING': '待付款',
    'PAID': '待发货',
    'SHIPPED': '已发货',
    'COMPLETED': '已完成',
    'CANCELED': '已取消'
  }
  
  return statusMap[status] || status
}

// 不再需要计算订单商品总数量，因为列表页不再显示订单项
const getTotalQuantity = () => {
  return 0; // 保留函数但不再使用
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return '--'
  const date = new Date(time)
  return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
}

// 加载订单数据
const loadOrders = async () => {
  loading.value = true;
  
  try {
    // 检查用户是否登录
    if (!userStore.isLoggedIn || !userStore.currentUser) {
      showToast('请先登录', 'error');
      router.push('/login');
      return;
    }
    
    const userId = userStore.currentUser.id;
    
    // 从API获取订单基本信息（不包含订单项）
    try {
      console.log('正在从API获取用户订单基本信息...');
      const apiOrders = await orderApi.getUserOrders(userId);
      console.log('从API获取到的订单数据:', apiOrders);
      
      // 处理嵌套数据，避免循环引用
      const processedOrders = apiOrders.map(order => {
        // 创建一个干净的订单对象
        const cleanOrder = {
          id: order.id,
          orderNumber: order.orderNumber,
          userId: order.userId,
          totalPrice: order.totalPrice,
          status: order.status,
          shippingAddress: order.shippingAddress,
          paymentMethod: order.paymentMethod,
          createTime: order.createdAt || order.createTime,
          updateTime: order.updatedAt || order.updateTime,
          reviewed: false // 默认未评价
        };
        
        // 处理订单项，避免使用嵌套的order对象
        if (order.items && Array.isArray(order.items)) {
          cleanOrder.items = order.items.map(item => {
            return {
              id: item.id,
              orderId: item.orderId,
              productId: item.productId,
              quantity: item.quantity,
              price: item.price,
              totalPrice: item.totalPrice,
              name: item.name || null,
              image: item.image || null
            };
          });
        } else {
          cleanOrder.items = [];
        }
        
        return cleanOrder;
      });
      
      orders.value = processedOrders;
      console.log('处理后的订单数据:', orders.value);
    } catch (error) {
      console.error('从API获取订单失败:', error);
      
      // 如果API请求失败，尝试从本地存储获取
      console.log('尝试从本地store获取订单数据...');
      orders.value = orderStore.orderList;
      console.log('从本地store获取到的订单数据:', orders.value);
    }
  } catch (error) {
    console.error('加载订单数据失败:', error);
    showToast('加载订单失败，请重试', 'error');
  } finally {
    loading.value = false;
  }
};

// 取消订单
const cancelOrder = async (orderId) => {
  if (!confirm('确定要取消这个订单吗？取消后无法恢复')) return;
  
  try {
    // 显示加载状态
    showToast('正在取消订单...', 'info');
    
    // 调用API取消订单
    await orderApi.cancelOrder(orderId);
    
    // 成功取消订单后
    // 1. 更新本地store中的订单状态
    orderStore.cancelOrder(orderId);
    
    // 2. 重新加载订单列表以获取最新状态
    await loadOrders();
    
    // 3. 显示成功消息
    showToast('订单已成功取消', 'success');
    
  } catch (error) {
    console.error('取消订单失败:', error);
    
    // 根据错误类型显示不同的错误消息
    if (error.response && error.response.data && error.response.data.error) {
      showToast(`取消订单失败: ${error.response.data.error}`, 'error');
    } else {
      showToast('取消订单失败，请重试', 'error');
    }
  }
};

// 确认收货
const confirmReceive = async (orderId) => {
  if (!confirm('确认已收到商品吗？')) return;
  
  try {
    // 先在本地Store中更新状态
    orderStore.confirmOrder(orderId);
    
    // 调用API确认收货
    try {
      await orderApi.confirmOrder(orderId);
      showToast('已确认收货', 'success');
    } catch (error) {
      console.error('调用API确认收货失败:', error);
      showToast('确认收货失败，但已在本地更新', 'warning');
    }
    
    // 重新加载订单列表
    loadOrders();
  } catch (error) {
    console.error('确认收货失败:', error);
    showToast('确认收货失败，请重试', 'error');
  }
};

// 删除订单
const deleteOrder = async (orderId) => {
  if (!confirm('确定要删除这个订单吗？删除后将无法恢复且无法查看相关信息')) return;
  
  try {
    // 显示加载状态
    showToast('正在删除订单...', 'info');
    
    // 调用API删除订单
    await orderApi.deleteOrder(orderId);
    
    // 成功删除订单后
    // 1. 从本地store中移除订单
    const updatedOrders = orders.value.filter(order => order.id !== orderId);
    orders.value = updatedOrders;
    
    // 2. 从orderStore中也移除
    orderStore.removeOrder(orderId);
    
    // 3. 显示成功消息
    showToast('订单已成功删除', 'success');
    
  } catch (error) {
    console.error('删除订单失败:', error);
    
    // 根据错误类型显示不同的错误消息
    if (error.response && error.response.data && error.response.data.error) {
      showToast(`删除订单失败: ${error.response.data.error}`, 'error');
    } else {
      showToast('删除订单失败，请重试', 'error');
    }
  }
};

// 前往评价页面
const goToReview = (orderId) => {
  // TODO: 跳转到评价页面
  showToast('评价功能暂未实现', 'info');
};

// 前往支付页面
const goToPay = async (order) => {
  try {
    // 显示加载状态
    showToast('正在准备支付信息...', 'info');
    
    // 准备一个干净的订单对象
    const basicOrderInfo = {
      id: order.id,
      orderNumber: order.orderNumber,
      userId: order.userId,
      totalPrice: order.totalPrice,
      status: order.status,
      shippingAddress: order.shippingAddress,
      paymentMethod: order.paymentMethod,
      recipientName: order.recipientName,
      recipientPhone: order.recipientPhone,
      createTime: order.createdAt || order.createTime,
      // 暂时不包含订单项
      items: []
    };
    
    // 如果需要详细订单信息（包括订单项），单独获取
    try {
      console.log('获取订单项信息:', order.id);
      // 获取订单项
      const orderItems = await orderApi.getOrderItems(order.id);
      console.log('获取到的订单项:', orderItems);
      
      // 只保留需要的订单项字段
      if (orderItems && Array.isArray(orderItems)) {
        basicOrderInfo.items = orderItems.map(item => ({
          id: item.id,
          productId: item.productId,
          quantity: item.quantity,
          price: item.price,
          totalPrice: item.price * item.quantity,
          // 可以从其他API获取产品信息，但这里简化处理
          name: `商品 #${item.productId}`,
          image: null
        }));
      }
    } catch (itemError) {
      console.error('获取订单项失败，将使用基本订单信息:', itemError);
      // 如果获取订单项失败，仍然可以继续，只是没有详细商品信息
    }
    
    console.log('处理后的简化订单数据:', basicOrderInfo);
    
    // 将简化的订单信息保存到sessionStorage，避免任何嵌套问题
    sessionStorage.setItem('currentOrder', JSON.stringify(basicOrderInfo));
    
    // 跳转到支付页面
    router.push(`/order/pay/${order.id}`);
  } catch (error) {
    console.error('准备支付信息失败:', error);
    showToast('无法准备支付信息，请重试', 'error');
  }
};

// 显示Toast消息
const showToast = (message, type = 'info', duration = 3000) => {
  // 清除之前的定时器
  if (toast.timer) {
    clearTimeout(toast.timer);
  }
  
  // 设置新的Toast内容
  toast.show = true;
  toast.message = message;
  toast.type = type;
  
  // 设置自动消失
  toast.timer = setTimeout(() => {
    toast.show = false;
  }, duration);
};

// 页面加载时获取订单数据
onMounted(() => {
  loadOrders();
});
</script>

<style scoped>
.orders-page {
  min-height: 100vh;
  padding-bottom: 40px;
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
  max-width: 1200px;
}

/* 加载状态 */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 300px;
}

.loading-spinner {
  font-size: 2rem;
  color: #409EFF;
  margin-bottom: 10px;
}

/* 标题样式与标签导航 */
.section-header {
  display: flex;
  flex-direction: column;
  margin-bottom: 30px;
}

.section-title {
  font-size: 1.5rem;
  font-weight: 600;
  color: #303133;
  margin: 0 0 20px 0;
}

.order-tabs {
  display: flex;
  border-bottom: 1px solid #ebeef5;
  padding-bottom: 10px;
}

.tab-item {
  padding: 8px 15px;
  margin-right: 15px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  color: #606266;
  transition: all 0.3s;
}

.tab-item:hover {
  color: #409EFF;
}

.tab-item.active {
  color: #409EFF;
  background-color: #ecf5ff;
}

/* 空订单样式 */
.empty-orders {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  padding: 60px 0;
  text-align: center;
  color: #909399;
}

.empty-orders i {
  font-size: 4rem;
  color: #dcdfe6;
  margin-bottom: 20px;
}

.go-shop {
  display: inline-block;
  margin-top: 20px;
  color: #409EFF;
  border: 1px solid #409EFF;
  padding: 8px 20px;
  border-radius: 20px;
  text-decoration: none;
  transition: all 0.3s;
}

.go-shop:hover {
  background-color: #409EFF;
  color: #fff;
}

/* 订单列表样式 */
.orders-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.order-card {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  background-color: #f5f7fa;
  border-bottom: 1px solid #ebeef5;
}

.order-info {
  display: flex;
  gap: 20px;
  font-size: 14px;
  color: #606266;
}

.order-status {
  font-weight: 600;
  font-size: 14px;
  padding: 5px 10px;
  border-radius: 4px;
}

.status-pending {
  color: #E6A23C;
  background-color: #fdf6ec;
}

.status-paid {
  color: #409EFF;
  background-color: #ecf5ff;
}

.status-shipped {
  color: #67C23A;
  background-color: #f0f9eb;
}

.status-completed {
  color: #67C23A;
  background-color: #f0f9eb;
}

.status-canceled {
  color: #909399;
  background-color: #f4f4f5;
}

.order-products {
  padding: 20px;
  border-bottom: 1px solid #ebeef5;
}

.product-item {
  display: flex;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px dashed #ebeef5;
}

.product-item:last-child {
  border-bottom: none;
}

.product-image {
  width: 80px;
  height: 80px;
  margin-right: 15px;
  border-radius: 4px;
  overflow: hidden;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
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
  font-size: 14px;
  color: #f56c6c;
}

.product-quantity {
  color: #909399;
  margin-left: 15px;
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
}

.order-total {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 5px;
  color: #606266;
  font-size: 14px;
}

.price {
  font-size: 14px;
}

.amount {
  font-size: 18px;
  font-weight: 600;
  color: #f56c6c;
}

.order-actions {
  display: flex;
  gap: 10px;
}

.btn {
  padding: 8px 12px;
  font-size: 13px;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid #dcdfe6;
  background-color: #fff;
  color: #606266;
  text-decoration: none;
  display: flex;
  align-items: center;
}

.btn i {
  margin-right: 5px;
}

.pay-btn {
  background-color: #f56c6c;
  border-color: #f56c6c;
  color: white;
}

.pay-btn:hover {
  background-color: #f78989;
  border-color: #f78989;
}

.cancel-btn:hover {
  color: #f56c6c;
  border-color: #fbc4c4;
}

.confirm-btn {
  background-color: #67C23A;
  border-color: #67C23A;
  color: white;
}

.confirm-btn:hover {
  background-color: #85ce61;
  border-color: #85ce61;
}

.track-btn:hover, 
.contact-btn:hover, 
.detail-btn:hover {
  color: #409EFF;
  border-color: #c6e2ff;
}

.review-btn {
  background-color: #E6A23C;
  border-color: #E6A23C;
  color: white;
}

.review-btn:hover {
  background-color: #ebb563;
  border-color: #ebb563;
}

.reviewed-btn {
  background-color: #909399;
  border-color: #909399;
  color: white;
  cursor: not-allowed;
}

.buy-again-btn:hover {
  color: #67C23A;
  border-color: #c2e7b0;
}

.delete-btn:hover {
  color: #F56C6C;
  border-color: #fbc4c4;
}

/* Toast提示样式 */
.toast {
  position: fixed;
  top: 20px;
  left: 50%;
  transform: translateX(-50%);
  padding: 10px 20px;
  border-radius: 4px;
  color: white;
  font-size: 14px;
  z-index: 9999;
  box-shadow: 0 3px 6px rgba(0, 0, 0, 0.2);
  animation: fadeIn 0.3s, fadeOut 0.3s 2.7s;
}

.toast.success {
  background-color: #67C23A;
}

.toast.error {
  background-color: #F56C6C;
}

.toast.warning {
  background-color: #E6A23C;
}

.toast.info {
  background-color: #909399;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translate(-50%, -20px); }
  to { opacity: 1; transform: translate(-50%, 0); }
}

@keyframes fadeOut {
  from { opacity: 1; transform: translate(-50%, 0); }
  to { opacity: 0; transform: translate(-50%, -20px); }
}

/* 响应式调整 */
@media (max-width: 768px) {
  .container {
    padding: 1rem;
    margin: 1rem;
  }
  
  .order-header, .order-footer {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .order-status {
    margin-top: 10px;
  }
  
  .order-info {
    flex-direction: column;
    gap: 5px;
  }
  
  .order-actions {
    margin-top: 15px;
    flex-wrap: wrap;
    justify-content: flex-end;
    width: 100%;
  }
  
  .order-total {
    width: 100%;
    align-items: flex-start;
    margin-bottom: 10px;
  }
}

/* 订单简要信息 */
.order-brief {
  padding: 15px 20px;
  border-bottom: 1px solid #ebeef5;
  display: flex;
  flex-direction: column;
  gap: 10px;
  color: #606266;
  font-size: 14px;
}

.order-address, .order-payment {
  display: flex;
  align-items: center;
}

.order-address i, .order-payment i {
  margin-right: 10px;
  color: #909399;
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
}
</style>