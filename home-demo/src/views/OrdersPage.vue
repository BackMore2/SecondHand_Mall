<template>
  <div class="orders-page">
    <!-- 导航栏组件保持不变，因为已经统一 -->
    <NavBar />
    
    <!-- 内容容器样式调整，与主页面保持一致 -->
    <div class="container">
      <div class="section-header">
        <h2 class="section-title">我的订单</h2>
      </div>
      
      <div v-if="orders.length === 0" class="empty-orders">
        <i class="fas fa-box"></i>
        <p>暂无订单</p>
        <router-link to="/" class="go-shop">去逛逛</router-link>
      </div>
      <div v-else class="content-container">
        <table class="orders-table">
          <thead>
            <tr>
              <th>订单号</th>
              <th>下单时间</th>
              <th>收货地址</th>
              <th>金额</th>
              <th>状态</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="order in orders" :key="order.id">
              <td>{{ order.id }}</td>
              <td>{{ formatTime(order.createTime) }}</td>
              <td>{{ order.address }}</td>
              <td>¥{{ order.total.toFixed(2) }}</td>
              <td>{{ order.status }}</td>
              <td>
                <router-link :to="`/orders/${order.id}`">详情</router-link>
                <span v-if="order.status === '待付款'">
                  | <router-link :to="`/order/pay/${order.id}`">去支付</router-link>
                  | <a href="javascript:void(0)" @click="cancel(order.id)">取消</a>
                </span>
                <span v-if="order.status === '已发货'">
                  | <a href="javascript:void(0)" @click="confirm(order.id)">确认收货</a>
                </span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>
<script setup>
import { useOrderStore } from '@/stores/order'
import { ref } from 'vue'
import NavBar from '@/components/NavBar.vue'

const orderStore = useOrderStore()
const orders = orderStore.orderList

function formatTime(time) {
  if (!time) return ''
  return new Date(time).toLocaleString()
}

function cancel(id) {
  if (confirm('确定取消该订单？')) orderStore.cancelOrder(id)
}

function confirm(id) {
  if (confirm('确认收货？')) orderStore.confirmOrder(id)
}
</script>
<style scoped>
.orders-page {
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
  max-width: 1200px;
}

/* 标题样式，与主页保持一致 */
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

/* 订单表格样式 */
.orders-table {
  width: 100%;
  border-collapse: collapse;
}

.orders-table th, .orders-table td {
  padding: 12px 15px;
  border-bottom: 1px solid #ebeef5;
  text-align: center;
}

.orders-table th {
  font-weight: 600;
  color: #606266;
  background-color: #f5f7fa;
}

.orders-table tr:hover {
  background-color: #f5f7fa;
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
</style>