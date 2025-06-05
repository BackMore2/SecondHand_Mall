<template>
  <div class="cart-page">
    <!-- 导航栏组件保持不变，因为已经统一 -->
    <NavBar />
    
    <!-- 内容容器样式调整，与订单页面保持一致 -->
    <div class="container">
      <div class="section-header">
        <h2 class="section-title">我的购物车</h2>
      </div>
      
      <div v-if="cart.items.length === 0" class="empty-cart">
        <i class="fas fa-shopping-cart"></i>
        <p>购物车为空</p>
        <router-link to="/" class="go-shop">去逛逛</router-link>
      </div>
      <div v-else class="content-container">
        <table class="cart-table">
          <thead>
            <tr>
              <th width="50"><input type="checkbox" :checked="cart.isAllSelected" @change="toggleAll($event)"></th>
              <th>商品信息</th>
              <th>单价</th>
              <th>数量</th>
              <th>小计</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in cart.items" :key="item.id">
              <td><input type="checkbox" v-model="item.selected" @change="cart.toggleSelect(item.id)"></td>
              <td>
                <div class="product-info" @click="viewProductDetail(item.id)">
                  <img :src="item.image" :alt="item.name" />
                  <div class="product-name">{{ item.name }}</div>
                </div>
              </td>
              <td>¥{{ item.price.toFixed(2) }}</td>
              <td>
                <div class="quantity-control">
                  <input type="number" v-model.number="item.quantity" @change="changeQty(item, 0)" min="1" />
                </div>
              </td>
              <td>¥{{ (item.price * item.quantity).toFixed(2) }}</td>
              <td>
                <a href="javascript:void(0)" @click="cart.removeFromCart(item.id)">删除</a>
              </td>
            </tr>
          </tbody>
        </table>
        
        <div class="cart-footer">
          <div class="cart-actions">
            <button class="clear-btn" @click="clearCartAll">
              清空购物车
            </button>
          </div>
          <div class="cart-total">
            <span class="selected-count">已选 <b>{{ cart.selectedItems.length }}</b> 件商品</span>
            <span class="total-price">合计：<span class="price">¥{{ cart.totalPrice.toFixed(2) }}</span></span>
            <button class="checkout-btn" :disabled="cart.selectedItems.length===0" @click="checkout">结算</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useCartStore } from '@/stores/cart'
import { useRouter } from 'vue-router'
import NavBar from '@/components/NavBar.vue'

const cart = useCartStore()
const router = useRouter()

function toggleAll(e) {
  cart.toggleSelectAll(e.target.checked)
}

function changeQty(item, delta) {
  if (delta !== 0) {
    cart.updateQuantity(item.id, item.quantity + delta)
  } else {
    cart.updateQuantity(item.id, item.quantity)
  }
}

function checkout() {
  // 获取选中商品，存储在sessionStorage中
  const selectedItems = cart.selectedItems;
  
  // 如果没有选中商品，提示用户
  if (selectedItems.length === 0) {
    alert('请至少选择一件商品');
    return;
  }
  
  // 将选中商品信息存储到sessionStorage中（避免刷新页面丢失数据）
  sessionStorage.setItem('checkoutItems', JSON.stringify(selectedItems));
  
  // 导航到订单创建页面
  router.push('/order/create');
}

function clearCartAll() {
  cart.items = [];
}

// 查看商品详情
function viewProductDetail(productId) {
  router.push(`/product/${productId}`);
}
</script>

<style scoped>
/* 整体页面样式 */
.cart-page {
  min-height: 100vh;
}

/* 内容容器样式，与订单页保持一致 */
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

/* 标题样式，与订单页保持一致 */
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
}

.section-title {
  font-size: 1.5rem;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

/* 内容容器卡片样式 */
.content-container {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  padding: 20px;
}

/* 购物车表格样式 */
.cart-table {
  width: 100%;
  border-collapse: collapse;
}

.cart-table th, .cart-table td {
  padding: 12px 15px;
  border-bottom: 1px solid #ebeef5;
  text-align: center;
}

.cart-table th {
  font-weight: 600;
  color: #606266;
  background-color: #f5f7fa;
}

.cart-table tr:hover {
  background-color: #f5f7fa;
}

/* 商品信息样式 */
.product-info {
  display: flex;
  align-items: center;
  text-align: left;
  cursor: pointer;
  transition: opacity 0.2s;
}

.product-info:hover {
  opacity: 0.8;
}

.product-info img {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 4px;
  margin-right: 10px;
}

.product-name {
  color: #303133;
}

/* 数量控制 */
.quantity-control {
  display: flex;
  align-items: center;
  justify-content: center;
}

.quantity-control input {
  width: 50px;
  text-align: center;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 5px;
}

/* 购物车底部 */
.cart-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 20px;
  padding-top: 15px;
  border-top: 1px solid #ebeef5;
}

.cart-actions {
  display: flex;
  gap: 10px;
}

.clear-btn {
  background: none;
  border: 1px solid #dcdfe6;
  color: #606266;
  padding: 8px 15px;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.clear-btn:hover {
  color: #f56c6c;
  border-color: #f56c6c;
}

.cart-total {
  display: flex;
  align-items: center;
  gap: 20px;
}

.selected-count {
  color: #606266;
}

.total-price {
  color: #606266;
}

.price {
  color: #f56c6c;
  font-size: 1.2rem;
  font-weight: bold;
}

.checkout-btn {
  background-color: #409EFF;
  color: white;
  border: none;
  border-radius: 4px;
  padding: 8px 24px;
  font-size: 1rem;
  cursor: pointer;
  transition: all 0.3s;
}

.checkout-btn:hover {
  background-color: #66b1ff;
}

.checkout-btn:disabled {
  background-color: #a0cfff;
  cursor: not-allowed;
}

/* 空购物车样式 */
.empty-cart {
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

.empty-cart i {
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
</style>