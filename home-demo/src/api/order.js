import api from './auth';

/**
 * 创建订单
 * @param {Object} orderData 订单数据
 * @returns {Promise<Object>} 创建的订单
 */
export const createOrder = async (orderData) => {
  try {
    // 确保有授权令牌
    const token = localStorage.getItem('token');
    if (!token) {
      throw new Error('未登录状态，无法创建订单');
    }
    
    console.log('发送订单数据:', JSON.stringify(orderData));
    
    // 检查并打印请求头信息，便于调试
    const headers = {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    };
    console.log('请求头:', headers);
    
    // 发送请求时添加更多日志
    const response = await api.post('/orders', orderData, {
      headers: headers
    });
    
    console.log('订单创建响应:', response.status, response.data);
    return response.data;
  } catch (error) {
    console.error('创建订单失败:', error);
    
    // 提供更详细的错误信息
    if (error.response) {
      console.error('订单创建错误响应:', {
        status: error.response.status,
        data: error.response.data
      });
      
      if (error.response.status === 403) {
        throw new Error('没有权限创建订单，请确认您已登录');
      }
    }
    
    throw error;
  }
};

/**
 * 获取用户的所有订单（只包含订单基本信息，不包含订单项）
 * @param {number} userId 用户ID
 * @returns {Promise<Array>} 订单列表
 */
export const getUserOrders = async (userId) => {
  try {
    // 添加查询参数指示不需要包含订单项
    const response = await api.get(`/orders/user/${userId}?includeItems=false`);
    return response.data;
  } catch (error) {
    console.error('获取订单列表失败:', error);
    throw error;
  }
};

/**
 * 获取订单详情（包含订单项）
 * @param {number} orderId 订单ID
 * @returns {Promise<Object>} 订单详情
 */
export const getOrderDetail = async (orderId) => {
  try {
    const response = await api.get(`/orders/${orderId}?includeItems=true`);
    return response.data;
  } catch (error) {
    console.error('获取订单详情失败:', error);
    throw error;
  }
};

/**
 * 获取订单项详情
 * @param {number} orderId 订单ID
 * @returns {Promise<Array>} 订单项列表
 */
export const getOrderItems = async (orderId) => {
  try {
    console.log(`正在获取订单 #${orderId} 的订单项...`);
    const response = await api.get(`/orders/${orderId}/items`);
    console.log(`成功获取订单项:`, response.data);
    return response.data;
  } catch (error) {
    console.error(`获取订单 #${orderId} 项目失败:`, error);
    throw error;
  }
};

/**
 * 取消订单
 * @param {number} orderId 订单ID
 * @returns {Promise<Object>} 操作结果
 */
export const cancelOrder = async (orderId) => {
  try {
    console.log(`正在取消订单: ${orderId}`);
    // 确保有授权令牌
    const token = localStorage.getItem('token');
    if (!token) {
      throw new Error('未登录状态，无法取消订单');
    }
    
    const headers = {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    };
    
    const response = await api.put(`/orders/${orderId}/cancel`, {}, {
      headers: headers
    });
    
    console.log('订单取消响应:', response.status, response.data);
    return response.data;
  } catch (error) {
    console.error('取消订单失败:', error);
    
    // 提供更详细的错误信息
    if (error.response) {
      console.error('订单取消错误响应:', {
        status: error.response.status,
        data: error.response.data
      });
    }
    
    throw error;
  }
};

/**
 * 支付订单
 * @param {number} orderId 订单ID
 * @param {Object} paymentData 支付信息
 * @returns {Promise<Object>} 操作结果
 */
export const payOrder = async (orderId, paymentData) => {
  try {
    const response = await api.put(`/orders/${orderId}/pay`, paymentData);
    return response.data;
  } catch (error) {
    console.error('支付订单失败:', error);
    throw error;
  }
};

/**
 * 确认收货
 * @param {number} orderId 订单ID
 * @returns {Promise<Object>} 操作结果
 */
export const confirmOrder = async (orderId) => {
  try {
    const response = await api.put(`/orders/${orderId}/confirm`);
    return response.data;
  } catch (error) {
    console.error('确认收货失败:', error);
    throw error;
  }
};

/**
 * 删除订单
 * @param {number} orderId 订单ID
 * @returns {Promise<Object>} 操作结果
 */
export const deleteOrder = async (orderId) => {
  try {
    console.log(`正在删除订单: ${orderId}`);
    // 确保有授权令牌
    const token = localStorage.getItem('token');
    if (!token) {
      throw new Error('未登录状态，无法删除订单');
    }
    
    const headers = {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    };
    
    const response = await api.delete(`/orders/${orderId}`, {
      headers: headers
    });
    
    console.log('订单删除响应:', response.status, response.data);
    return response.data;
  } catch (error) {
    console.error('删除订单失败:', error);
    
    // 提供更详细的错误信息
    if (error.response) {
      console.error('订单删除错误响应:', {
        status: error.response.status,
        data: error.response.data
      });
    }
    
    throw error;
  }
};