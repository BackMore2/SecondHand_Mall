<template>
  <div class="cart-page">
    <!-- Toast提示组件 -->
    <transition name="toast-fade">
      <div v-if="toastVisible" :class="['toast', `toast-${toastType}`]">
        {{ toastMessage }}
      </div>
    </transition>
    
    <!-- 导航栏组件 -->
    <NavBar />
    
    <!-- 内容容器 -->
    <div class="container">
      <div class="section-header">
        <h2 class="section-title">我的购物车</h2>
        <div class="header-actions">
          <div class="sync-status" v-if="cart.userId">
            <span v-if="cart.isSync" class="status-synced">
              <i class="fas fa-check-circle"></i> 已同步
            </span>
            <span v-else class="status-local">
              <i class="fas fa-exclamation-circle"></i> 本地模式
            </span>
          </div>
          <button @click="refreshCart" class="refresh-btn" :disabled="cart.loading || isRefreshing">
            <i class="fas fa-sync-alt" :class="{ 'fa-spin': isRefreshing || cart.loading }"></i> 刷新
          </button>
        </div>
      </div>
      
      <!-- 加载中 -->
      <div v-if="cart.loading || cart.productLoading" class="loading-container">
        <i class="fas fa-spinner fa-spin"></i>
        <p>{{ cart.loading ? '正在加载购物车数据...' : '正在更新商品信息...' }}</p>
      </div>
      
      <!-- 错误提示 -->
      <div v-else-if="cart.error" class="error-container">
        <i class="fas fa-exclamation-circle"></i>
        <p>{{ cart.error }}</p>
        <button @click="retryLoad" class="retry-btn">重新加载</button>
      </div>
      
      <!-- 空购物车 -->
      <div v-else-if="cart.items.length === 0" class="empty-cart">
        <i class="fas fa-shopping-cart"></i>
        <p>购物车为空</p>
        <router-link to="/" class="go-shop">去逛逛</router-link>
      </div>
      
      <!-- 购物车内容 -->
      <div v-else class="content-container">
        <!-- 商品状态提示 -->
        <div v-if="outOfStockItems.length > 0" class="stock-alert-banner">
          <i class="fas fa-exclamation-triangle"></i>
          <span>您的购物车中有 {{ outOfStockItems.length }} 件商品已售罄，已自动取消勾选</span>
        </div>
        
        <table class="cart-table">
          <thead>
            <tr>
              <th width="50">
                <input 
                  type="checkbox" 
                  :checked="cart.isAllSelected" 
                  @change="toggleAll($event)"
                >
              </th>
              <th>商品信息</th>
              <th>单价</th>
              <th>数量</th>
              <th>小计</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in cart.items" :key="item.id" :class="{'out-of-stock': item.stock <= 0}">
              <td>
                <input 
                  type="checkbox" 
                  v-model="item.selected" 
                  @change="cart.toggleSelect(item.id)"
                  :disabled="item.stock <= 0"
                >
              </td>
              <td>
                <div class="product-info" @click="viewProductDetail(item.id)">
                  <img :src="item.image" :alt="item.name" />
                  <div class="product-info-content">
                    <div class="product-name">{{ item.name }}</div>
                    <div v-if="item.stock <= 0" class="out-of-stock-label">已售罄</div>
                    <div v-else-if="item.stock < 5" class="low-stock-label">库存紧张</div>
                  </div>
                </div>
              </td>
              <td>¥{{ item.price.toFixed(2) }}</td>
              <td>
                <div class="quantity-control">
                  <button 
                    class="qty-btn minus" 
                    @click="changeQty(item, -1)"
                    :disabled="item.quantity <= 1 || item.updating || item.stock <= 0"
                  >-</button>
                  <input 
                    type="number" 
                    v-model.number="item.quantity" 
                    @change="changeQty(item, 0)" 
                    min="1" 
                    :max="item.stock"
                    :disabled="item.updating || item.stock <= 0"
                  />
                  <button 
                    class="qty-btn plus" 
                    @click="changeQty(item, 1)"
                    :disabled="item.quantity >= item.stock || item.updating || item.stock <= 0"
                  >+</button>
                  <span v-if="item.updating" class="updating-indicator">
                    <i class="fas fa-spinner fa-spin"></i>
                  </span>
                </div>
              </td>
              <td>¥{{ (item.price * item.quantity).toFixed(2) }}</td>
              <td>
                <button 
                  class="remove-btn" 
                  @click="removeItem(item.id)"
                  :disabled="item.removing"
                >
                  <i v-if="item.removing" class="fas fa-spinner fa-spin"></i>
                  <span v-else>删除</span>
                </button>
              </td>
            </tr>
          </tbody>
        </table>
        
        <div class="cart-footer">
          <div class="cart-actions">
            <button 
              class="clear-btn" 
              @click="confirmClearCart"
              :disabled="cart.operationInProgress"
            >
              清空购物车
            </button>
            <button 
              class="check-stock-btn" 
              @click="checkOutOfStock"
              :disabled="cart.operationInProgress"
            >
              检查库存
            </button>
          </div>
          <div class="cart-total">
            <span class="selected-count">
              已选 <b>{{ cart.selectedItems.length }}</b> 件商品，共计 <b>{{ totalSelectedQuantity }}</b> 件
            </span>
            <span class="total-price">
              合计：<span class="price">¥{{ cart.totalPrice.toFixed(2) }}</span>
            </span>
            <button 
              class="checkout-btn" 
              :disabled="cart.selectedItems.length === 0 || cart.operationInProgress" 
              @click="checkout"
            >
              结算
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watchEffect } from 'vue';
import { useCartStore } from '@/stores/cart';
import { useRouter } from 'vue-router';
import NavBar from '@/components/NavBar.vue';

const cart = useCartStore();
const router = useRouter();
const isRefreshing = ref(false); // 手动刷新状态

// Toast提示相关
const toastMessage = ref('');
const toastVisible = ref(false);
const toastType = ref('info'); // success, error, info
let toastTimer = null;

// 计算选中商品的总数量
const totalSelectedQuantity = computed(() => {
  return cart.selectedItems.reduce((sum, item) => sum + item.quantity, 0);
});

// 售罄商品列表
const outOfStockItems = computed(() => {
  return cart.outOfStockItems;
});

// 初始化加载购物车数据
onMounted(async () => {
  await refreshCart();
  // 检查售罄商品
  checkOutOfStock(false);
});

// 强制刷新购物车数据和商品详情
async function refreshCart(showLoading = true) {
  if (showLoading) {
    isRefreshing.value = true;
  }
  try {
    // 初始化购物车数据
    await cart.initCart();
    
    // 如果购物车有商品，刷新商品详情
    if (cart.items.length > 0) {
      await cart.updateProductDetails();
    }
  } catch (error) {
    showToast('刷新购物车失败，请稍后重试', 'error');
  } finally {
    if (showLoading) {
      isRefreshing.value = false;
    }
  }
}

// 重新加载购物车
async function retryLoad() {
  await refreshCart();
}

// 全选/取消全选
function toggleAll(e) {
  cart.toggleSelectAll(e.target.checked);
}

// 检查售罄商品并处理
function checkOutOfStock(showToasts = true) {
  const result = cart.checkOutOfStockItems();
  
  if (showToasts) {
    if (result.outOfStockItems.length > 0) {
      showToast(`已将 ${result.outOfStockItems.length} 件售罄商品取消选择`, 'info');
    }
    
    if (result.lowStockItems.length > 0) {
      showToast(`已将 ${result.lowStockItems.length} 件商品的数量调整至库存上限`, 'info');
    }
    
    if (result.outOfStockItems.length === 0 && result.lowStockItems.length === 0) {
      showToast('所有商品库存正常', 'success');
    }
  }
}

// 修改商品数量
async function changeQty(item, delta) {
  if (item.updating) return;
  
  let newQuantity = item.quantity;
  
  if (delta !== 0) {
    newQuantity = item.quantity + delta;
  }
  
  // 确保数量不小于1且不超过库存
  newQuantity = Math.max(1, Math.min(newQuantity, item.stock || 1));
  
  if (newQuantity !== item.quantity) {
    // 设置正在更新状态
    item.updating = true;
    
    try {
      await cart.updateQuantity(item.id, newQuantity);
      showToast('数量已更新', 'success');
    } catch (error) {
      console.error('更新数量失败:', error);
      showToast('更新数量失败，请稍后重试', 'error');
    } finally {
      // 重置更新状态
      setTimeout(() => {
        item.updating = false;
      }, 300);
    }
  }
}

// 从购物车移除商品
async function removeItem(itemId) {
  const item = cart.items.find(i => i.id === itemId);
  if (!item || item.removing) return;
  
  if (confirm('确定从购物车中移除此商品？')) {
    // 设置正在移除状态
    item.removing = true;
    
    try {
      await cart.removeFromCart(itemId);
      showToast('商品已从购物车移除', 'success');
    } catch (error) {
      console.error('移除商品失败:', error);
      showToast('移除商品失败，请稍后重试', 'error');
      
      // 重置移除状态
      item.removing = false;
    }
  }
}

// 清空购物车
async function confirmClearCart() {
  if (cart.items.length === 0) {
    showToast('购物车已经是空的', 'info');
    return;
  }
  
  if (confirm('确定清空购物车？此操作不可撤销。')) {
    try {
      console.log('开始清空购物车, 当前购物车项数量:', cart.items.length);
      console.log('当前用户ID:', cart.userId);
      
      // 显示加载提示
      showToast('正在清空购物车...', 'info');
      
      // 调用store方法清空购物车
      await cart.clearCart();
      
      console.log('购物车清空成功, 当前购物车项数量:', cart.items.length);
      showToast('购物车已清空', 'success');
      
      // 刷新一次购物车数据
      setTimeout(() => {
        refreshCart(false);
      }, 500);
    } catch (error) {
      console.error('清空购物车失败:', error);
      
      // 提供更详细的错误信息
      let errorMessage = '清空购物车失败';
      if (error && error.message) {
        errorMessage += ': ' + error.message;
      }
      
      showToast(errorMessage, 'error');
      
      // 尝试重新初始化购物车
      try {
        await cart.initCart();
      } catch (e) {
        console.error('重新初始化购物车失败:', e);
      }
    }
  }
}

// 结算
function checkout() {
  // 获取选中商品，存储在sessionStorage中
  const selectedItems = cart.selectedItems;
  
  // 如果没有选中商品，提示用户
  if (selectedItems.length === 0) {
    showToast('请至少选择一件商品', 'error');
    return;
  }
  
  // 将选中商品信息存储到sessionStorage中
  sessionStorage.setItem('checkoutItems', JSON.stringify(selectedItems));
  
  // 导航到订单创建页面
  router.push('/order/create');
}

// 查看商品详情
function viewProductDetail(productId) {
  router.push(`/product/${productId}`);
}

// Toast提示函数
function showToast(message, type = 'info', duration = 2000) {
  // 清除之前的定时器
  if (toastTimer) {
    clearTimeout(toastTimer);
  }
  
  // 设置新消息
  toastMessage.value = message;
  toastType.value = type;
  toastVisible.value = true;
  
  // 设置自动消失
  toastTimer = setTimeout(() => {
    toastVisible.value = false;
  }, duration);
}
</script>

<style scoped>
/* 整体页面样式 */
.cart-page {
  min-height: 100vh;
  /* 移除背景色以显示全局渐变背景 */
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

/* 内容容器样式 */
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

/* 标题样式 */
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
}

/* 商品状态提示 */
.stock-alert-banner {
  background-color: #FFF7E6;
  border-left: 3px solid #E6A23C;
  padding: 10px 15px;
  margin-bottom: 15px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  font-size: 14px;
  color: #E6A23C;
}

.stock-alert-banner i {
  margin-right: 8px;
}

/* 更新指示器 */
.updating-indicator {
  margin-left: 5px;
  color: #409EFF;
  font-size: 14px;
}

/* 状态标签 */
.low-stock-label {
  color: #E6A23C;
  font-size: 0.8rem;
}

/* 检查库存按钮 */
.check-stock-btn {
  padding: 8px 15px;
  background-color: #E6A23C;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
  margin-left: 10px;
}

.check-stock-btn:hover:not(:disabled) {
  background-color: #F2B661;
}

.check-stock-btn:disabled {
  background-color: #EBEEF5;
  color: #909399;
  cursor: not-allowed;
}

/* 头部操作 */
.header-actions {
  display: flex;
  align-items: center;
  gap: 15px;
}

.refresh-btn {
  background: none;
  border: none;
  color: #409EFF;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 0.9rem;
  padding: 5px 10px;
  border-radius: 4px;
  transition: all 0.3s;
}

.refresh-btn:hover:not(:disabled) {
  background-color: rgba(64, 158, 255, 0.1);
}

.refresh-btn:disabled {
  color: #909399;
  cursor: not-allowed;
}

.status-synced {
  color: #67C23A;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.status-local {
  color: #E6A23C;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 购物车表格样式 */
.cart-table {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 20px;
}

.cart-table th,
.cart-table td {
  padding: 12px;
  text-align: center;
  border-bottom: 1px solid #EBEEF5;
  vertical-align: middle;
}

.cart-table thead th {
  background-color: #F5F7FA;
  color: #606266;
  font-weight: 500;
}

/* 商品信息样式 */
.product-info {
  display: flex;
  align-items: center;
  text-align: left;
  cursor: pointer;
}

.product-info img {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 4px;
  border: 1px solid #EBEEF5;
  margin-right: 10px;
}

.product-info-content {
  flex: 1;
}

.product-name {
  font-size: 14px;
  margin-bottom: 5px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.out-of-stock-label {
  color: #F56C6C;
  font-size: 12px;
}

/* 数量控制样式 */
.quantity-control {
  display: inline-flex;
  align-items: center;
  border: 1px solid #DCDFE6;
  border-radius: 4px;
  overflow: hidden;
}

.qty-btn {
  width: 28px;
  height: 28px;
  border: none;
  background-color: #F5F7FA;
  color: #606266;
  cursor: pointer;
}

.qty-btn:hover:not(:disabled) {
  background-color: #EBEEF5;
}

.qty-btn:disabled {
  color: #C0C4CC;
  cursor: not-allowed;
}

.quantity-control input {
  width: 40px;
  height: 28px;
  text-align: center;
  border: none;
  border-left: 1px solid #DCDFE6;
  border-right: 1px solid #DCDFE6;
  outline: none;
}

/* 移除按钮样式 */
.remove-btn {
  padding: 5px 10px;
  border: none;
  background-color: transparent;
  color: #909399;
  cursor: pointer;
  border-radius: 4px;
  transition: all 0.3s;
}

.remove-btn:hover:not(:disabled) {
  color: #F56C6C;
  background-color: rgba(245, 108, 108, 0.1);
}

.remove-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 购物车底部 */
.cart-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 20px;
  border-top: 1px solid #EBEEF5;
}

/* 清空按钮 */
.clear-btn {
  padding: 8px 15px;
  background-color: #F56C6C;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.clear-btn:hover:not(:disabled) {
  background-color: #F78989;
}

.clear-btn:disabled {
  background-color: #EBEEF5;
  color: #909399;
  cursor: not-allowed;
}

/* 购物车总计 */
.cart-total {
  display: flex;
  align-items: center;
}

.selected-count {
  margin-right: 20px;
  font-size: 14px;
  color: #606266;
}

.total-price {
  font-size: 14px;
  color: #606266;
  margin-right: 15px;
}

.total-price .price {
  font-size: 18px;
  color: #F56C6C;
  font-weight: bold;
}

/* 结算按钮 */
.checkout-btn {
  padding: 10px 30px;
  background-color: #F56C6C;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.checkout-btn:hover:not(:disabled) {
  background-color: #F78989;
}

.checkout-btn:disabled {
  background-color: #EBEEF5;
  color: #909399;
  cursor: not-allowed;
}

/* 售罄商品行样式 */
.out-of-stock {
  background-color: #FFF7E6;
  opacity: 0.8;
}

/* 加载容器 */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
  color: #909399;
}

.loading-container i {
  font-size: 30px;
  margin-bottom: 10px;
  color: #409EFF;
}

/* 错误容器 */
.error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
  color: #F56C6C;
}

.error-container i {
  font-size: 30px;
  margin-bottom: 10px;
}

.retry-btn {
  margin-top: 15px;
  padding: 8px 20px;
  background-color: #409EFF;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.retry-btn:hover {
  background-color: #66B1FF;
}

/* 空购物车 */
.empty-cart {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px;
}

.empty-cart i {
  font-size: 60px;
  color: #DCDFE6;
  margin-bottom: 15px;
}

.empty-cart p {
  font-size: 16px;
  color: #909399;
  margin-bottom: 20px;
}

.go-shop {
  padding: 10px 20px;
  background-color: #409EFF;
  color: white;
  text-decoration: none;
  border-radius: 4px;
  transition: all 0.3s;
}

.go-shop:hover {
  background-color: #66B1FF;
}
</style>