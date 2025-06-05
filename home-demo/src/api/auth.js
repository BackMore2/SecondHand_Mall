import axios from 'axios';

// 创建 axios 实例
const api = axios.create({
  baseURL: 'http://localhost:8080/api', // 后端 API 的基础 URL
  timeout: 5000, // 请求超时时间
  headers: {
    'Content-Type': 'application/json'
  }
});

// 登录
export const login = async (username, password) => {
  try {
    const response = await api.post('/auth/login', {
      username,
      password
    });
    return response.data;
  } catch (error) {
    throw error;
  }
};

// 注册
export const register = async (userData) => {
  try {
    const response = await api.post('/auth/register', userData);
    return response.data;
  } catch (error) {
    throw error;
  }
};

// 拦截器：添加 token 到请求头
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

export default api; 