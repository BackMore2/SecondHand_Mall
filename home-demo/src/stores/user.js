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
      return state.currentUser?.avatar || '/avatars/default-avatar.png';
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
        
        // 验证旧密码 (模拟)
        const hashedOldPassword = this.hashPassword(oldPassword);
        // 实际实现中应验证旧密码是否正确
        
        // 设置新密码
        const hashedNewPassword = this.hashPassword(newPassword);
        
        // 模拟API调用 - 实际项目中替换为真实的API调用
        return { success: true, message: '密码修改成功' };
      } catch (error) {
        console.error('Password change failed:', error);
        return { success: false, error: '密码修改失败' };
      }
    },
    
    // 获取用户地址列表
    async fetchUserAddresses() {
      try {
        if (!this.currentUser) return { success: false, error: '用户未登录' };
        
        // 模拟API调用 - 实际项目中替换为真实的API调用
        this.addresses = [
          {
            id: 'addr_1',
            name: '张三',
            phone: '13800138000',
            province: '广东省',
            city: '深圳市',
            district: '南山区',
            address: '科技园路1号',
            isDefault: true
          },
          {
            id: 'addr_2',
            name: '李四',
            phone: '13900139000',
            province: '北京市',
            city: '北京市',
            district: '海淀区',
            address: '中关村南大街5号',
            isDefault: false
          }
        ];
        
        return { success: true, addresses: this.addresses };
      } catch (error) {
        console.error('Fetch addresses failed:', error);
        return { success: false, error: '获取地址列表失败' };
      }
    },
    
    // 添加新地址
    async addAddress(addressData) {
      try {
        if (!this.currentUser) return { success: false, error: '用户未登录' };
        
        // 生成唯一ID
        const newAddress = {
          id: 'addr_' + Date.now(),
          ...addressData
        };
        
        // 如果是默认地址，将其他地址设为非默认
        if (newAddress.isDefault) {
          this.addresses.forEach(addr => {
            addr.isDefault = false;
          });
        }
        
        // 添加到地址列表
        this.addresses.push(newAddress);
        
        return { success: true, address: newAddress };
      } catch (error) {
        console.error('Add address failed:', error);
        return { success: false, error: '添加地址失败' };
      }
    },
    
    // 更新地址
    async updateAddress(addressId, addressData) {
      try {
        if (!this.currentUser) return { success: false, error: '用户未登录' };
        
        const index = this.addresses.findIndex(addr => addr.id === addressId);
        if (index === -1) return { success: false, error: '地址不存在' };
        
        // 如果是默认地址，将其他地址设为非默认
        if (addressData.isDefault) {
          this.addresses.forEach(addr => {
            addr.isDefault = false;
          });
        }
        
        // 更新地址
        this.addresses[index] = { ...this.addresses[index], ...addressData };
        
        return { success: true, address: this.addresses[index] };
      } catch (error) {
        console.error('Update address failed:', error);
        return { success: false, error: '更新地址失败' };
      }
    },
    
    // 删除地址
    async deleteAddress(addressId) {
      try {
        if (!this.currentUser) return { success: false, error: '用户未登录' };
        
        const index = this.addresses.findIndex(addr => addr.id === addressId);
        if (index === -1) return { success: false, error: '地址不存在' };
        
        // 删除地址
        this.addresses.splice(index, 1);
        
        // 如果删除的是默认地址且还有其他地址，将第一个地址设为默认
        if (this.addresses.length > 0 && !this.addresses.some(addr => addr.isDefault)) {
          this.addresses[0].isDefault = true;
        }
        
        return { success: true };
      } catch (error) {
        console.error('Delete address failed:', error);
        return { success: false, error: '删除地址失败' };
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