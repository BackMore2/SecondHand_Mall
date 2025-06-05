import { defineStore } from 'pinia';

export const useOrderStore = defineStore('order', {
  state: () => ({
    orders: [], // 订单列表
    nextOrderId: 1
  }),
  actions: {
    createOrder(order) {
      order.id = this.nextOrderId++;
      order.status = '待付款';
      order.createTime = new Date().toISOString();
      this.orders.unshift(order);
      return order.id;
    },
    payOrder(orderId) {
      const order = this.orders.find(o => o.id === orderId);
      if (order && order.status === '待付款') {
        order.status = '待发货';
        order.payTime = new Date().toISOString();
      }
    },
    cancelOrder(orderId) {
      const order = this.orders.find(o => o.id === orderId);
      if (order && (order.status === '待付款' || order.status === '待发货')) {
        order.status = '已取消';
        order.cancelTime = new Date().toISOString();
      }
    },
    confirmOrder(orderId) {
      const order = this.orders.find(o => o.id === orderId);
      if (order && order.status === '已发货') {
        order.status = '已完成';
        order.confirmTime = new Date().toISOString();
      }
    },
    shipOrder(orderId) {
      const order = this.orders.find(o => o.id === orderId);
      if (order && order.status === '待发货') {
        order.status = '已发货';
        order.shipTime = new Date().toISOString();
      }
    }
  },
  getters: {
    getOrderById: (state) => (id) => state.orders.find(o => o.id === Number(id)),
    orderList: (state) => state.orders
  },
  persist: {
    enabled: true,
    strategies: [
      { key: 'orders', storage: localStorage }
    ]
  }
}); 