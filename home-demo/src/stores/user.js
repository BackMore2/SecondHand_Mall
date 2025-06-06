import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import CryptoJS from 'crypto-js';
import axios from 'axios';

// 创建 axios 实例
const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 5000,
  headers: {
    'Content-Type': 'application/json'
  }
});

// 添加请求拦截器
api.interceptors.request.use(
  (config) => {
    // 从 localStorage 获取 token
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

// 添加响应拦截器
api.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    if (error.response?.status === 401) {
      // token 过期或无效，清除本地存储并跳转到登录页
      localStorage.removeItem('token');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export const useUserStore = defineStore('user', {
  state: () => ({
    currentUser: null,
    token: localStorage.getItem('token'), // 初始化时从 localStorage 获取 token
    addresses: [],
    isLoggedIn: !!localStorage.getItem('token'), // 根据 token 是否存在判断登录状态
  }),

  getters: {
    // 获取用户头像，如无则使用默认头像
    userAvatar: (state) => {
      if (!state.currentUser?.avatar) {
        console.log('没有头像，使用默认头像');
        return '/avatars/default-avatar.png';
      }
      
      // 输出头像路径，用于调试
      console.log('原始头像路径:', state.currentUser.avatar);
      
      // 如果头像路径是完整的 URL，直接返回
      if (state.currentUser.avatar.startsWith('http')) {
        console.log('使用完整URL头像');
        return state.currentUser.avatar;
      }
      
      // 如果是以 /uploads/ 开头的路径，添加服务器基础URL
      if (state.currentUser.avatar.startsWith('/uploads/')) {
        const avatarUrl = `http://localhost:8080${state.currentUser.avatar}`;
        console.log('完整头像URL:', avatarUrl);
        return avatarUrl;
      }
      
      // 其他情况，可能是相对路径，也添加服务器基础URL
      const avatarUrl = `http://localhost:8080${state.currentUser.avatar}`;
      console.log('完整头像URL:', avatarUrl);
      return avatarUrl;
    },
    // 获取用户全名
    fullName: (state) => {
      if (!state.currentUser) return '';
      return `${state.currentUser.firstName || ''} ${state.currentUser.lastName || ''}`.trim();
    }
  },

  actions: {
    // 登录方法
    async login(loginData) {
      try {
        const response = await api.post('/auth/login', {
          credential: loginData.credential,
          password: this.hashPassword(loginData.password)  // 恢复使用 hashPassword
        });

        if (response.data) {
          // 保存 token 到 localStorage
          if (response.data.token) {
            localStorage.setItem('token', response.data.token);
            this.token = response.data.token;
          }

          // 更新用户信息
          this.currentUser = response.data.user || response.data;
          this.isLoggedIn = true;
          
          // 获取用户地址
          await this.fetchUserAddresses();
          
          return { success: true, user: this.currentUser };
        }
        return { success: false, error: '登录失败，请检查账号和密码' };
      } catch (error) {
        console.error('Login failed:', error);
        return { 
          success: false, 
          error: error.response?.data || '登录失败，请检查您的账号和密码'
        };
      }
    },
    
    // 注册方法
    async register(userData) {
      try {
        // 验证数据
        if (!userData.username || !userData.password) {
          return { success: false, error: '请填写必要的注册信息' };
        }
        
        // 加密密码
        const hashedPassword = this.hashPassword(userData.password);
        
        const response = await api.post('/auth/register', {
          ...userData,
          password: hashedPassword
        });

        if (response.data) {
          // 更新状态
          this.currentUser = response.data;
          this.isLoggedIn = true;
          
          return { success: true, user: response.data };
        }
        return { success: false, error: '注册失败，请稍后重试' };
      } catch (error) {
        console.error('Registration failed:', error);
        return { 
          success: false, 
          error: error.response?.data || '注册失败，请稍后重试'
        };
      }
    },
    
    // 注销方法
    logout() {
      this.currentUser = null;
      this.token = null;
      this.addresses = [];
      this.isLoggedIn = false;
      localStorage.removeItem('token');
    },
    
    // 更新用户个人资料
    async updateProfile(profileData) {
      try {
        if (!this.currentUser) return { success: false, error: '用户未登录' };
        
        // 调用后端 API
        const response = await api.put(`/users/${this.currentUser.id}/profile`, {
          username: profileData.username,
          email: profileData.email,
          phone: profileData.phone
        });

        if (response.data) {
          // 更新状态
          this.currentUser = {
            ...this.currentUser,
            ...response.data
          };
          
          return { success: true, user: this.currentUser };
        }
        return { success: false, error: '更新个人资料失败' };
      } catch (error) {
        console.error('Profile update failed:', error);
        return { 
          success: false, 
          error: error.response?.data?.message || '更新个人资料失败'
        };
      }
    },
    
    // 更新用户头像
    async updateAvatar(avatarFile) {
      try {
        if (!this.currentUser) return { success: false, error: '用户未登录' };
        
        // 模拟头像上传 - 实际项目中替换为真实的文件上传API
        // 这里假设成功，返回一个模拟的URL
        const avatarUrl = `/avatars/user_${this.currentUser.id}_${Date.now()}.jpg`;
        
        this.currentUser.avatar = avatarUrl;
        
        return { success: true, avatarUrl };
      } catch (error) {
        console.error('Avatar update failed:', error);
        return { success: false, error: '头像上传失败' };
      }
    },
    
    // 密码重置请求
    async requestPasswordReset(email) {
      try {
        // 模拟API调用 - 实际项目中替换为真实的API调用
        // 假设发送了重置密码邮件
        return { success: true, message: '密码重置链接已发送到您的邮箱' };
      } catch (error) {
        console.error('Password reset request failed:', error);
        return { success: false, error: '密码重置请求失败' };
      }
    },
    
    // 修改密码
    async changePassword(oldPassword, newPassword) {
      try {
        if (!this.currentUser) return { success: false, error: '用户未登录' };
        
        // 调用后端 API
        const response = await api.post(`/users/${this.currentUser.id}/change-password`, {
          oldPassword: this.hashPassword(oldPassword),
          newPassword: this.hashPassword(newPassword)
        });

        if (response.status === 200) {
          return { success: true, message: '密码修改成功' };
        }
        return { success: false, error: '密码修改失败' };
      } catch (error) {
        console.error('Password change failed:', error);
        return { 
          success: false, 
          error: error.response?.data?.error || '密码修改失败'
        };
      }
    },
    
    // 获取用户地址列表
    async fetchUserAddresses() {
      try {
        if (!this.currentUser) return { success: false, error: '用户未登录' };
        
        // 调用后端 API
        const response = await api.get(`/addresses/user/${this.currentUser.id}`);
        
        if (response.data) {
          this.addresses = response.data;
          return { success: true, addresses: this.addresses };
        }
        return { success: false, error: '获取地址列表失败' };
      } catch (error) {
        console.error('Fetch addresses failed:', error);
        return { 
          success: false, 
          error: error.response?.data?.error || '获取地址列表失败'
        };
      }
    },
    
    // 添加新地址
    async addAddress(addressData) {
      try {
        if (!this.currentUser) return { success: false, error: '用户未登录' };
        
        // 调用后端 API
        const response = await api.post(`/addresses/user/${this.currentUser.id}`, {
          recipientName: addressData.name,
          recipientPhone: addressData.phone,
          address: `${addressData.province} ${addressData.city} ${addressData.district} ${addressData.address}`,
          isDefault: addressData.isDefault
        });
        
        if (response.data) {
          // 更新地址列表
          await this.fetchUserAddresses();
          return { success: true, address: response.data };
        }
        return { success: false, error: '添加地址失败' };
      } catch (error) {
        console.error('Add address failed:', error);
        return { 
          success: false, 
          error: error.response?.data?.error || '添加地址失败'
        };
      }
    },
    
    // 更新地址
    async updateAddress(addressId, addressData) {
      try {
        if (!this.currentUser) return { success: false, error: '用户未登录' };
        
        // 如果只传了isDefault字段（设置默认地址）
        if (Object.keys(addressData).length === 1 && 'isDefault' in addressData) {
          try {
            const response = await api.put(`/addresses/${addressId}/default`);
            if (response.data) {
              // 更新地址列表
              await this.fetchUserAddresses();
              return { success: true, address: response.data };
            }
            return { success: false, error: '设置默认地址失败' };
          } catch (error) {
            console.error('Set default address failed:', error);
            throw error;
          }
        }
        
        // 确保token在请求头中
        const token = localStorage.getItem('token');
        if (!token) {
          return { success: false, error: '用户未登录或会话已过期' };
        }
        
        console.log('更新地址请求数据:', {
          addressId,
          recipientName: addressData.name,
          recipientPhone: addressData.phone,
          address: `${addressData.province} ${addressData.city} ${addressData.district} ${addressData.address}`,
          isDefault: addressData.isDefault,
          user: { id: this.currentUser.id }
        });
        
        // 调用后端 API
        try {
          const response = await api.put(`/addresses/${addressId}`, {
            recipientName: addressData.name,
            recipientPhone: addressData.phone,
            address: `${addressData.province} ${addressData.city} ${addressData.district} ${addressData.address}`,
            isDefault: addressData.isDefault,
            user: { id: this.currentUser.id } // 添加用户信息
          });
          
          console.log('更新地址响应:', response);
          
          if (response.data) {
            // 更新地址列表
            await this.fetchUserAddresses();
            return { success: true, address: response.data };
          }
          return { success: false, error: '更新地址失败' };
        } catch (error) {
          console.error('Update address API call failed:', error);
          
          // 即使API调用失败，我们也刷新一下地址列表，因为数据库可能已经更新了
          try {
            await this.fetchUserAddresses();
          } catch (fetchError) {
            console.error('Failed to refresh addresses after update:', fetchError);
          }
          
          throw error;
        }
      } catch (error) {
        console.error('Update address failed:', error);
        // 详细记录错误信息，便于调试
        if (error.response) {
          console.error('Error status:', error.response.status);
          console.error('Error data:', error.response.data);
        }
        return { 
          success: false, 
          error: error.response?.data?.error || '更新地址失败'
        };
      }
    },
    
    // 删除地址
    async deleteAddress(addressId) {
      try {
        if (!this.currentUser) return { success: false, error: '用户未登录' };
        
        // 调用后端 API
        const response = await api.delete(`/addresses/${addressId}`);
        
        if (response.status === 200) {
          // 更新地址列表
          await this.fetchUserAddresses();
          return { success: true };
        }
        return { success: false, error: '删除地址失败' };
      } catch (error) {
        console.error('Delete address failed:', error);
        return { 
          success: false, 
          error: error.response?.data?.error || '删除地址失败'
        };
      }
    },
    
    // 密码加密辅助方法
    hashPassword(password) {
      return CryptoJS.SHA256(password).toString();
    }
  },
  
  // 持久化存储
  persist: {
    enabled: true,
    strategies: [
      {
        key: 'user',
        storage: localStorage,
        paths: ['currentUser', 'token', 'isLoggedIn', 'addresses']
      }
    ]
  }
}); 