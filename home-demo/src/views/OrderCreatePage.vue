<template>
  <div class="order-create-page">
    <!-- Toast提示组件 -->
    <transition name="toast-fade">
      <div v-if="toastVisible" :class="['toast', `toast-${toastType}`]">
        {{ toastMessage }}
      </div>
    </transition>
    
    <!-- 地址选择模态框 -->
    <div class="modal" v-if="showAddressModal">
      <div class="modal-content">
        <div class="modal-header">
          <h3>选择收货地址</h3>
          <span class="close-btn" @click="showAddressModal = false">&times;</span>
        </div>
        <div class="modal-body">
          <div v-if="loading" class="loading">
            <i class="fas fa-spinner fa-spin"></i>
            <p>加载地址中...</p>
          </div>
          <div v-else-if="addressList.length === 0" class="empty-address">
            <p>您还没有添加收货地址</p>
            <button class="add-address-btn" @click="showAddAddressForm = true; showAddressModal = false">添加地址</button>
          </div>
          <div v-else class="address-list">
            <div 
              v-for="(address, index) in addressList" 
              :key="address.id"
              class="address-item"
              :class="{ 'active': selectedAddressId === address.id }"
              @click="selectAddress(address)"
            >
              <div class="address-info">
                <div class="recipient">
                  <span class="name">{{ address.recipientName }}</span>
                  <span class="phone">{{ address.recipientPhone }}</span>
                  <span v-if="address.isDefault" class="default-tag">默认</span>
                </div>
                <div class="address-text">{{ formatAddress(address) }}</div>
              </div>
              <div class="address-actions">
                <button class="use-btn" @click.stop="useAddress(address)">使用</button>
              </div>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="add-address-btn" @click="showAddAddressForm = true; showAddressModal = false">添加新地址</button>
          <button class="cancel-btn" @click="showAddressModal = false">取消</button>
        </div>
      </div>
    </div>
    
    <!-- 添加地址表单 -->
    <div class="modal" v-if="showAddAddressForm">
      <div class="modal-content">
        <div class="modal-header">
          <h3>添加收货地址</h3>
          <span class="close-btn" @click="showAddAddressForm = false">&times;</span>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>收货人</label>
            <input type="text" v-model="newAddress.recipientName" placeholder="请输入收货人姓名" />
          </div>
          <div class="form-group">
            <label>手机号</label>
            <input type="text" v-model="newAddress.recipientPhone" placeholder="请输入联系电话" />
          </div>
          <div class="form-group">
            <label>省份</label>
            <input type="text" v-model="newAddress.province" placeholder="请输入省份" />
          </div>
          <div class="form-group">
            <label>城市</label>
            <input type="text" v-model="newAddress.city" placeholder="请输入城市" />
          </div>
          <div class="form-group">
            <label>区/县</label>
            <input type="text" v-model="newAddress.district" placeholder="请输入区/县" />
          </div>
          <div class="form-group">
            <label>详细地址</label>
            <textarea v-model="newAddress.detailAddress" placeholder="请输入详细地址"></textarea>
          </div>
          <div class="form-group checkbox">
            <input type="checkbox" id="setDefault" v-model="newAddress.isDefault">
            <label for="setDefault">设为默认地址</label>
          </div>
        </div>
        <div class="modal-footer">
          <button class="save-btn" @click="saveAddress">保存</button>
          <button class="cancel-btn" @click="showAddAddressForm = false">取消</button>
        </div>
      </div>
    </div>
    
    <!-- 添加导航栏 -->
    <NavBar />
    
    <div class="content-container">
      <h2>创建订单</h2>
      <div class="order-form">
        <!-- 收货信息 -->
        <div class="section">
          <h3>收货信息</h3>
          <div v-if="selectedAddress" class="selected-address">
            <div class="address-info">
              <div class="recipient">
                <span class="name">{{ selectedAddress.recipientName }}</span>
                <span class="phone">{{ selectedAddress.recipientPhone }}</span>
                <span v-if="selectedAddress.isDefault" class="default-tag">默认</span>
              </div>
              <div class="address-text">{{ formatAddress(selectedAddress) }}</div>
          </div>
            <button class="change-address-btn" @click="showAddressModal = true">更换地址</button>
          </div>
          <div v-else class="no-address">
            <p>还没有选择收货地址</p>
            <button class="select-address-btn" @click="showAddressModal = true">选择地址</button>
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
import { useUserStore } from '@/stores/user'
import * as addressApi from '@/api/address'
import * as orderApi from '@/api/order'
import NavBar from '@/components/NavBar.vue'

// 路由器
const router = useRouter()

// 获取store
const orderStore = useOrderStore()
const cartStore = useCartStore()
const userStore = useUserStore()

// 地址选择相关
const showAddressModal = ref(false)
const showAddAddressForm = ref(false)
const addressList = ref([])
const selectedAddress = ref(null)
const selectedAddressId = ref(null)
const loading = ref(false)

// 新增地址表单
const newAddress = ref({
  recipientName: '',
  recipientPhone: '',
  province: '',
  city: '',
  district: '',
  detailAddress: '',
  isDefault: false
})

// Toast提示相关
const toastMessage = ref('')
const toastVisible = ref(false)
const toastType = ref('info') // success, error, info
let toastTimer = null

// 订单信息
const orderInfo = ref({
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
onMounted(async () => {
  try {
    // 检查用户登录状态
    if (!userStore.isLoggedIn) {
      showToast('请先登录', 'error')
      router.push('/login')
      return
    }

    // 加载结算商品
    const checkoutItems = sessionStorage.getItem('checkoutItems')
    if (checkoutItems) {
      products.value = JSON.parse(checkoutItems)
    } else {
      // 如果没有结算商品，返回购物车页
      router.push('/cart')
      return
    }

    // 加载用户收货地址
    await loadAddresses()
  } catch (error) {
    console.error('初始化订单页面失败:', error)
    showToast('加载数据失败，请重试', 'error')
  }
})

// 加载用户收货地址
async function loadAddresses() {
  try {
    loading.value = true
    const userId = userStore.currentUser.id
    
    console.log('正在加载用户地址，用户ID:', userId)
    const addresses = await addressApi.getUserAddresses(userId)
    addressList.value = addresses
    
    console.log('加载到地址数量:', addresses.length)
    
    // 如果有默认地址，自动选择
    const defaultAddress = addresses.find(addr => addr.isDefault)
    if (defaultAddress) {
      selectedAddress.value = defaultAddress
      selectedAddressId.value = defaultAddress.id
      console.log('已自动选择默认地址:', defaultAddress.id)
    } else if (addresses.length > 0) {
      // 否则选择第一个地址
      selectedAddress.value = addresses[0]
      selectedAddressId.value = addresses[0].id
      console.log('已自动选择第一个地址:', addresses[0].id)
  }
  } catch (error) {
    console.error('加载地址失败:', error)
    showToast('加载地址失败', 'error')
  } finally {
    loading.value = false
  }
}

// 格式化地址显示
function formatAddress(address) {
  if (!address) return ''
  // 如果地址是完整的字符串（后端格式），直接返回
  if (address.address) {
    return address.address
  }
  // 如果地址是拆分的格式（前端格式），组合返回
  if (address.province || address.city || address.district || address.detailAddress) {
    return `${address.province || ''} ${address.city || ''} ${address.district || ''} ${address.detailAddress || ''}`.trim()
  }
  return ''
}

// 选择地址
function selectAddress(address) {
  selectedAddressId.value = address.id
}

// 使用选择的地址
function useAddress(address) {
  selectedAddress.value = address
  selectedAddressId.value = address.id
  showAddressModal.value = false
}

// 保存新地址
async function saveAddress() {
  try {
    // 验证表单
    if (!newAddress.value.recipientName || !newAddress.value.recipientPhone || 
        !newAddress.value.province || !newAddress.value.city || 
        !newAddress.value.district || !newAddress.value.detailAddress) {
      showToast('请填写完整的地址信息', 'error')
      return
    }
    
    loading.value = true
    const userId = userStore.currentUser.id
    
    // 创建地址对象，后端需要完整地址字符串而不是分开的字段
    const addressData = {
      recipientName: newAddress.value.recipientName,
      recipientPhone: newAddress.value.recipientPhone,
      address: `${newAddress.value.province} ${newAddress.value.city} ${newAddress.value.district} ${newAddress.value.detailAddress}`,
      isDefault: newAddress.value.isDefault
    }
    
    // 保留原始字段用于前端显示
    addressData.province = newAddress.value.province
    addressData.city = newAddress.value.city
    addressData.district = newAddress.value.district
    addressData.detailAddress = newAddress.value.detailAddress
    
    // 调用API保存地址
    const savedAddress = await addressApi.addAddress(userId, addressData)
    
    // 更新地址列表
    addressList.value.push(savedAddress)
    
    // 如果是默认地址或者是第一个地址，自动选择
    if (savedAddress.isDefault || addressList.value.length === 1) {
      selectedAddress.value = savedAddress
      selectedAddressId.value = savedAddress.id
    }
    
    // 重置表单
    newAddress.value = {
      recipientName: '',
      recipientPhone: '',
      province: '',
      city: '',
      district: '',
      detailAddress: '',
      isDefault: false
    }
    
    showAddAddressForm.value = false
    showToast('地址添加成功', 'success')
  } catch (error) {
    console.error('保存地址失败:', error)
    showToast('保存地址失败', 'error')
  } finally {
    loading.value = false
  }
}

// 计算总价
const totalPrice = computed(() => {
  return products.value.reduce((sum, item) => sum + item.price * item.quantity, 0)
})

// 运费
const shipping = ref(10)

// 提交订单
async function submitOrder() {
  try {
    // 验证是否选择了地址
    if (!selectedAddress.value) {
      showToast('请选择收货地址', 'error')
      return
    }
    
    // 显示加载状态
    loading.value = true
    
    // 获取用户ID和令牌
    const userId = userStore.currentUser.id
    const token = localStorage.getItem('token')
    
    if (!token) {
      showToast('用户未登录，请先登录', 'error')
      router.push('/login')
    return
  }
  
  // 创建订单对象
    const orderData = {
      userId: userId,
      // 确保地址信息正确传递
      shippingAddress: selectedAddress.value.address || formatAddress(selectedAddress.value),
      recipientName: selectedAddress.value.recipientName,
      recipientPhone: selectedAddress.value.recipientPhone,
      totalPrice: totalPrice.value + shipping.value,
      paymentMethod: orderInfo.value.paymentMethod,
      status: "PENDING",
      // 添加订单项列表
      items: products.value.map(item => ({
        productId: item.id,
        quantity: item.quantity,
        price: item.price,
        totalPrice: item.price * item.quantity
      }))
    }
    
    // 输出日志，便于调试
    console.log('订单数据:', orderData)
    
    // 调用API创建订单
    const result = await orderApi.createOrder(orderData)
    console.log('订单创建成功:', result)
    
    // 同步到本地store，保存订单信息
    const orderId = orderStore.createOrder({
      ...result,
      // 确保所有必要的订单信息都保存下来
      items: products.value,
      recipientName: selectedAddress.value.recipientName,
      recipientPhone: selectedAddress.value.recipientPhone,
      shippingAddress: selectedAddress.value.address || formatAddress(selectedAddress.value),
      paymentMethod: orderInfo.value.paymentMethod,
      totalPrice: totalPrice.value + shipping.value,
      shipping: shipping.value
    })
    
    // 将完整订单信息保存到sessionStorage以确保页面刷新后仍可获取
    sessionStorage.setItem('currentOrder', JSON.stringify({
      id: orderId,
      ...result,
      items: products.value,
      recipientName: selectedAddress.value.recipientName,
      recipientPhone: selectedAddress.value.recipientPhone,
      shippingAddress: selectedAddress.value.address || formatAddress(selectedAddress.value),
      paymentMethod: orderInfo.value.paymentMethod,
      totalPrice: totalPrice.value + shipping.value,
      shipping: shipping.value
    }))
    
    // 清除购物车中已下单的商品
    const itemIds = products.value.map(item => item.id)
    for (const id of itemIds) {
      await cartStore.removeFromCart(id)
    }
    
    // 清除sessionStorage中的结算商品
    sessionStorage.removeItem('checkoutItems')
    
    // 显示成功提示
    showToast('订单创建成功，即将跳转到支付页面', 'success')
    
    // 跳转到支付页面
    setTimeout(() => {
      router.push(`/order/pay/${result.id}`)
    }, 1500)
  } catch (error) {
    console.error('创建订单失败:', error)
    
    // 提供更详细的错误消息
    let errorMessage = '创建订单失败'
    if (error.response) {
      if (error.response.status === 403) {
        errorMessage = '权限不足，无法创建订单，请确认您已登录'
      } else if (error.response.data && error.response.data.message) {
        errorMessage = `创建订单失败: ${error.response.data.message}`
      } else if (error.response.data && error.response.data.error) {
        errorMessage = `创建订单失败: ${error.response.data.error}`
      }
    } else if (error.message) {
      errorMessage = error.message
    }
    
    showToast(errorMessage, 'error')
    
    // 如果是认证问题，尝试重新登录
    if (error.response && error.response.status === 403) {
      console.log('认证问题，可能需要重新登录')
      setTimeout(() => {
        router.push('/login')
      }, 2000)
    }
  } finally {
    loading.value = false
  }
}

// Toast提示函数
function showToast(message, type = 'info', duration = 2000) {
  // 清除之前的定时器
  if (toastTimer) {
    clearTimeout(toastTimer)
  }
  
  // 设置新消息
  toastMessage.value = message
  toastType.value = type
  toastVisible.value = true
  
  // 设置自动消失
  toastTimer = setTimeout(() => {
    toastVisible.value = false
  }, duration)
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

/* Toast提示样式 */
.toast {
  position: fixed;
  top: 20px;
  left: 50%;
  transform: translateX(-50%);
  padding: 10px 20px;
  border-radius: 4px;
  z-index: 1000;
  font-size: 14px;
  color: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
  max-width: 80%;
}

.toast-success {
  background-color: #67C23A;
}

.toast-error {
  background-color: #F56C6C;
}

.toast-info {
  background-color: #409EFF;
}

.toast-fade-enter-active,
.toast-fade-leave-active {
  transition: opacity 0.3s, transform 0.3s;
}

.toast-fade-enter-from,
.toast-fade-leave-to {
  opacity: 0;
  transform: translateX(-50%) translateY(-20px);
}

/* 模态框样式 */
.modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  width: 90%;
  max-width: 600px;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.modal-header {
  padding: 15px 20px;
  border-bottom: 1px solid #eee;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-header h3 {
  margin: 0;
  font-size: 18px;
}

.close-btn {
  font-size: 24px;
  background: none;
  border: none;
  cursor: pointer;
  color: #999;
}

.modal-body {
  padding: 20px;
  overflow-y: auto;
  max-height: 60vh;
}

.modal-footer {
  padding: 15px 20px;
  border-top: 1px solid #eee;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

/* 地址列表样式 */
.address-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.address-item {
  border: 1px solid #ddd;
  border-radius: 4px;
  padding: 15px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
  transition: all 0.3s;
}

.address-item:hover {
  border-color: #409EFF;
  background-color: #f0f9ff;
}

.address-item.active {
  border-color: #409EFF;
  background-color: #f0f9ff;
}

.address-info {
  flex: 1;
}

.recipient {
  margin-bottom: 5px;
}

.name {
  font-weight: bold;
  margin-right: 10px;
}

.phone {
  color: #666;
}

.default-tag {
  background-color: #409EFF;
  color: white;
  padding: 2px 5px;
  border-radius: 2px;
  font-size: 12px;
  margin-left: 5px;
}

.address-text {
  color: #666;
  font-size: 14px;
}

.address-actions {
  display: flex;
  gap: 5px;
}

.use-btn {
  background-color: #409EFF;
  color: white;
  border: none;
  padding: 5px 10px;
  border-radius: 4px;
  cursor: pointer;
}

/* 按钮样式 */
.add-address-btn, .save-btn {
  background-color: #409EFF;
  color: white;
  border: none;
  padding: 8px 15px;
  border-radius: 4px;
  cursor: pointer;
}

.cancel-btn {
  background-color: #f5f5f5;
  color: #666;
  border: 1px solid #ddd;
  padding: 8px 15px;
  border-radius: 4px;
  cursor: pointer;
}

/* 加载中样式 */
.loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 30px 0;
}

.loading i {
  font-size: 24px;
  color: #409EFF;
  margin-bottom: 10px;
}

/* 空地址提示 */
.empty-address {
  text-align: center;
  padding: 30px 0;
}

.empty-address p {
  margin-bottom: 15px;
  color: #999;
}

/* 选中的地址样式 */
.selected-address {
  border: 1px solid #ddd;
  border-radius: 4px;
  padding: 15px;
  margin-bottom: 15px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.change-address-btn {
  background-color: #f5f5f5;
  color: #666;
  border: 1px solid #ddd;
  padding: 5px 10px;
  border-radius: 4px;
  cursor: pointer;
}

/* 未选择地址提示 */
.no-address {
  text-align: center;
  padding: 20px;
  background-color: #f9f9f9;
  border-radius: 4px;
  margin-bottom: 15px;
}

.no-address p {
  margin-bottom: 10px;
  color: #999;
}

.select-address-btn {
  background-color: #409EFF;
  color: white;
  border: none;
  padding: 8px 15px;
  border-radius: 4px;
  cursor: pointer;
}

/* 复选框样式 */
.form-group.checkbox {
  display: flex;
  align-items: center;
  gap: 5px;
}

.form-group.checkbox input {
  width: auto;
}

.form-group.checkbox label {
  display: inline;
  margin: 0;
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