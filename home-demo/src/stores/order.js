import { defineStore } from 'pinia';

export const useOrderStore = defineStore('order', {
  state: () => ({
    orders: [], // 订单列表
    nextOrderId: 1
  }),
  actions: {
    createOrder(order) {
      // 如果没有指定ID，则自动生成一个
      if (!order.id) {
      order.id = this.nextOrderId++;
      } else if (order.id >= this.nextOrderId) {
        // 确保nextOrderId始终大于已存在的最大ID
        this.nextOrderId = order.id + 1;
      }
      
      // 设置默认状态和创建时间（如果未指定）
      if (!order.status) {
      order.status = '待付款';
      }
      if (!order.createTime) {
      order.createTime = new Date().toISOString();
      }
      
      // 检查订单是否已存在，如果存在则更新它
      const existingOrderIndex = this.orders.findIndex(o => o.id === order.id);
      if (existingOrderIndex >= 0) {
        this.orders[existingOrderIndex] = { ...order };
      } else {
        // 如果不存在，则添加到订单列表开头
      this.orders.unshift(order);
      }
      
      console.log('保存订单到store:', order);
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
      // 放宽条件，不仅是已发货的订单，待发货的订单也可以直接确认为已完成
      // 这样从支付页面可以直接将订单标记为已完成
      if (order && (order.status === '已发货' || order.status === '待发货' || 
          order.status === 'SHIPPED' || order.status === 'PAID')) {
        order.status = '已完成';
        order.confirmTime = new Date().toISOString();
        console.log(`订单 #${orderId} 已更新为已完成状态`);
      } else {
        console.warn(`无法确认订单 #${orderId}: 当前状态 ${order?.status || '未知'} 不允许此操作`);
      }
    },
    shipOrder(orderId) {
      const order = this.orders.find(o => o.id === orderId);
      if (order && order.status === '待发货') {
        order.status = '已发货';
        order.shipTime = new Date().toISOString();
      }
    },
    removeOrder(orderId) {
      const orderIndex = this.orders.findIndex(o => o.id === orderId);
      if (orderIndex !== -1) {
        this.orders.splice(orderIndex, 1);
        console.log(`已从本地store中删除订单ID: ${orderId}`);
        return true;
      }
      console.warn(`尝试删除不存在的订单ID: ${orderId}`);
      return false;
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