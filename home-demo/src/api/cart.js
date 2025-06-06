import api from './auth';

/**
 * 获取用户购物车信息
 * @param {number} userId 用户ID
 * @returns {Promise<Object>} 购物车信息
 */
export const getCart = async (userId) => {
  try {
    // 确保token存在
    const token = localStorage.getItem('token');
    if (!token) {
      console.error('未登录状态，无法获取购物车');
      throw new Error('用户未登录');
    }
    
    const response = await api.get(`/cart/${userId}`);
    return response.data;
  } catch (error) {
    console.error('获取购物车失败:', error);
    throw error;
  }
};

/**
 * 添加商品到购物车
 * @param {number} userId 用户ID
 * @param {number} productId 商品ID
 * @param {number} quantity 数量
 * @returns {Promise<Object>} 更新后的购物车信息
 */
export const addToCart = async (userId, productId, quantity = 1) => {
  try {
    // 确保token存在
    const token = localStorage.getItem('token');
    if (!token) {
      console.error('未登录状态，无法添加到购物车');
      throw new Error('用户未登录');
    }
    
    // 使用请求体传递数据而不是查询参数
    const response = await api.post('/cart/add', {
      userId,
      productId,
      quantity
    });
    return response.data;
  } catch (error) {
    console.error('添加商品到购物车失败:', error);
    throw error;
  }
};

/**
 * 更新购物车商品数量
 * @param {number} cartItemId 购物车项ID
 * @param {number} quantity 新数量
 * @returns {Promise<Object>} 更新后的购物车信息
 */
export const updateCartItemQuantity = async (cartItemId, quantity) => {
  try {
    // 确保token存在
    const token = localStorage.getItem('token');
    if (!token) {
      console.error('未登录状态，无法更新购物车');
      throw new Error('用户未登录');
    }
    
    // 使用请求体传递数据而不是查询参数
    const response = await api.put(`/cart/item/${cartItemId}`, {
      quantity
    });
    return response.data;
  } catch (error) {
    console.error('更新购物车商品数量失败:', error);
    throw error;
  }
};

/**
 * 从购物车移除商品
 * @param {number} cartItemId 购物车项ID
 * @returns {Promise<void>}
 */
export const removeFromCart = async (cartItemId) => {
  try {
    // 确保token存在
    const token = localStorage.getItem('token');
    if (!token) {
      console.error('未登录状态，无法从购物车移除商品');
      throw new Error('用户未登录');
    }
    
    await api.delete(`/cart/item/${cartItemId}`);
  } catch (error) {
    console.error('从购物车移除商品失败:', error);
    throw error;
  }
};

/**
 * 清空用户购物车
 * @param {number} userId 用户ID
 * @returns {Promise<void>}
 */
export const clearCart = async (userId) => {
  try {
    console.log('API: 开始清空购物车, 用户ID:', userId);
    
    // 确保token存在
    const token = localStorage.getItem('token');
    if (!token) {
      console.error('未登录状态，无法清空购物车');
      throw new Error('用户未登录');
    }
    
    // 设置超时时间更长
    const response = await api.delete(`/cart/clear/${userId}`, {
      timeout: 10000, // 增加超时时间到10秒
      headers: {
        'Cache-Control': 'no-cache', // 禁用缓存
        'Pragma': 'no-cache'
      }
    });
    
    console.log('API: 清空购物车响应:', response.status, response.data);
    return response.data;
  } catch (error) {
    console.error('API: 清空购物车失败:', error);
    
    // 提供更详细的错误信息
    if (error.response) {
      console.error('API错误响应:', {
        status: error.response.status,
        data: error.response.data,
        headers: error.response.headers
      });
      
      // 包装错误，保留原始错误信息
      throw new Error(`服务器返回错误(${error.response.status}): ${JSON.stringify(error.response.data)}`);
    } else if (error.request) {
      console.error('API无响应:', error.request);
      throw new Error('服务器无响应，请检查网络连接');
    }
    
    throw error;
  }
};

/**
 * 格式化购物车数据，从后端数据转换为前端需要的格式
 * @param {Object} cartData 后端返回的购物车数据
 * @returns {Array} 格式化后的购物车商品列表
 */
export const formatCartData = (cartData) => {
  // 如果购物车为空，返回空数组
  if (!cartData || !cartData.items || !Array.isArray(cartData.items)) {
    return [];
  }
  
  // 将购物车项格式化为前端需要的格式
  return cartData.items.map(item => ({
    id: item.productId,
    cartItemId: item.id,
    quantity: item.quantity,
    selected: true, // 默认选中
    name: item.productName,
    price: item.productPrice,
    image: item.productImage,
    stock: item.productStock
  }));
}; 