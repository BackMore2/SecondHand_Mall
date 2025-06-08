import axios from 'axios';

// 创建 axios 实例 - 不带token的专用实例
const api = axios.create({
  baseURL: 'http://localhost:8080', // 后端 API 的基础 URL
  timeout: 5000, // 请求超时时间
  headers: {
    'Content-Type': 'application/json'
  }
});

// 注意：这个API实例不添加token拦截器，确保请求不会携带token

/**
 * 发送消息到AI客服
 * @param {string} message 用户消息
 * @returns {Promise<Object>} AI的回复
 */
export const sendMessageToAI = async (message) => {
  try {
    // 直接请求 /customerService/chat 路径，不需要添加/api前缀
    const response = await api.post('api/customerService/chat', { message });
    return response.data;
  } catch (error) {
    console.error('AI客服请求失败:', error);
    throw error;
  }
}; 