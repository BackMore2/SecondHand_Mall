import api from './auth';
import { getProductRating } from './review';

// 检查并输出当前认证状态（调试用）
export const checkAuth = () => {
  const token = localStorage.getItem('token');
  console.log('当前Token状态:', token ? '已存在' : '不存在');
  if (token) {
    console.log('Token值 (前10个字符):', token.substring(0, 10) + '...');
  }
  return token;
};

// 获取所有商品
export const getAllProducts = async () => {
  try {
    // 输出认证状态
    checkAuth();
    const response = await api.get('/products');
    
    // 获取所有商品的评分信息
    const products = response.data.content || response.data;
    
    // 为每个商品添加评分信息
    if (Array.isArray(products) && products.length > 0) {
      for (const product of products) {
        try {
          const ratingData = await getProductRating(product.id);
          product.averageRating = ratingData.averageRating || 0;
          product.reviewCount = ratingData.reviewCount || 0;
        } catch (error) {
          console.error(`获取商品 ${product.id} 的评分信息失败:`, error);
          product.averageRating = 0;
          product.reviewCount = 0;
        }
      }
    }
    
    return response.data;
  } catch (error) {
    throw error;
  }
};

// 获取商品详情
export const getProductById = async (id) => {
  try {
    const response = await api.get(`/products/${id}`);
    
    // 获取商品评分信息
    try {
      const ratingData = await getProductRating(id);
      response.data.averageRating = ratingData.averageRating || 0;
      response.data.reviewCount = ratingData.reviewCount || 0;
    } catch (error) {
      console.error(`获取商品 ${id} 的评分信息失败:`, error);
      response.data.averageRating = 0;
      response.data.reviewCount = 0;
    }
    
    return response.data;
  } catch (error) {
    throw error;
  }
};

// 获取用户商品列表
export const getUserProducts = async () => {
  try {
    // 输出认证状态
    checkAuth();
    const response = await api.get('/products/user');
    return response.data;
  } catch (error) {
    throw error;
  }
};

// 添加商品
export const addProduct = async (productData) => {
  try {
    // 输出认证状态
    const token = checkAuth();
    if (!token) {
      console.error('添加商品失败: 未登录状态');
      throw new Error('用户未登录，请先登录');
    }
    
    // 处理图片数据
    const formattedData = formatProductData(productData);
    
    console.log('发送商品数据 (不包含图片内容):', {
      ...formattedData, 
      images: formattedData.images ? '包含图片数据...' : undefined
    });
    
    // 输出请求信息
    console.log('发送请求到:', api.defaults.baseURL + '/products');
    console.log('使用的认证头:', `Bearer ${token.substring(0, 10)}...`);
    
    // 确保请求头中包含 Authorization
    const headers = {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    };
    
    const response = await api.post('/products', formattedData, { headers });
    return response.data;
  } catch (error) {
    console.error('添加商品请求失败:', error.response?.status, error.response?.statusText);
    console.error('错误详情:', error.response?.data || error.message);
    
    // 处理特定错误
    if (error.response) {
      if (error.response.status === 401) {
        throw new Error('认证失败，请重新登录');
      } else if (error.response.status === 403) {
        throw new Error('权限不足，无法执行此操作');
      }
    }
    
    throw error;
  }
};

// 更新商品
export const updateProduct = async (id, productData) => {
  try {
    // 处理图片数据
    const formattedData = formatProductData(productData);
    
    const response = await api.put(`/products/${id}`, formattedData);
    return response.data;
  } catch (error) {
    throw error;
  }
};

// 更新商品状态（上架/下架）
export const updateProductStatus = async (id, online) => {
  try {
    const response = await api.put(`/products/${id}/status?online=${online}`);
    return response.data;
  } catch (error) {
    throw error;
  }
};

// 删除商品
export const deleteProduct = async (id) => {
  try {
    const response = await api.delete(`/products/${id}`);
    return response.data;
  } catch (error) {
    throw error;
  }
};

// 上传商品图片
export const uploadProductImage = async (imageFile) => {
  try {
    const formData = new FormData();
    formData.append('image', imageFile);
    
    const response = await api.post('/upload/image', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });
    
    return response.data;
  } catch (error) {
    throw error;
  }
};

// 辅助函数：格式化商品数据
function formatProductData(productData) {
  const formattedData = { ...productData };
  
  // 将图片数组转换为JSON字符串（保留Base64数据）
  if (Array.isArray(formattedData.images)) {
    formattedData.images = JSON.stringify(formattedData.images);
    
    // 设置主图
    if (formattedData.images.length > 0) {
      formattedData.main_image = formattedData.images[0];
    }
  }
  
  // 处理交易方式
  if (formattedData.supportMethods) {
    formattedData.face_to_face = formattedData.supportMethods.faceToFace ? 1 : 0;
    formattedData.delivery = formattedData.supportMethods.delivery ? 1 : 0;
    delete formattedData.supportMethods;
  }
  
  return formattedData;
} 