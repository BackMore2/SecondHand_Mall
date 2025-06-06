import api from './auth';

/**
 * 获取商品的评价列表
 * @param {number} productId 商品ID
 * @returns {Promise<Array>} 商品评价列表
 */
export const getProductReviews = async (productId) => {
  try {
    const response = await api.get(`/reviews/product/${productId}`);
    return response.data;
  } catch (error) {
    console.error('获取商品评价失败:', error);
    throw error;
  }
};

/**
 * 添加商品评价
 * @param {Object} reviewData 评价数据
 * @returns {Promise<Object>} 创建的评价数据
 */
export const addReview = async (reviewData) => {
  try {
    const response = await api.post('/reviews', reviewData);
    return response.data;
  } catch (error) {
    console.error('添加评价失败:', error);
    throw error;
  }
};

/**
 * 获取用户的评价列表
 * @param {number} userId 用户ID
 * @returns {Promise<Array>} 用户评价列表
 */
export const getUserReviews = async (userId) => {
  try {
    const response = await api.get(`/reviews/user/${userId}`);
    return response.data;
  } catch (error) {
    console.error('获取用户评价失败:', error);
    throw error;
  }
};

/**
 * 处理评价中的图片 - 将JSON字符串转换为数组
 * @param {Object} review 评价对象
 * @returns {Object} 处理后的评价对象
 */
export const normalizeReview = (review) => {
  const normalizedReview = { ...review };
  
  // 处理图片（将JSON字符串转换为数组）
  if (typeof normalizedReview.images === 'string' && normalizedReview.images) {
    try {
      normalizedReview.images = JSON.parse(normalizedReview.images);
    } catch {
      normalizedReview.images = [];
    }
  } else if (!Array.isArray(normalizedReview.images)) {
    normalizedReview.images = [];
  }
  
  // 处理评价内容
  normalizedReview.content = normalizedReview.comment || normalizedReview.content || '';
  
  // 转换时间格式
  if (normalizedReview.createdAt) {
    normalizedReview.date = new Date(normalizedReview.createdAt);
  } else if (normalizedReview.createTime) {
    normalizedReview.date = new Date(normalizedReview.createTime);
  } else {
    normalizedReview.date = new Date();
  }
  
  return normalizedReview;
}; 