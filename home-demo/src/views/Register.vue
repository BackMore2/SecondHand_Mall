<template>
  <div class="register-page">
    <div class="register-container">
      <div class="register-box">
        <div class="register-header">
          <h1>校园二手交易平台</h1>
          <h2>欢迎加入</h2>
          <p>创建一个账号开始交易</p>
        </div>
        
        <form @submit.prevent="handleRegister" class="register-form">
          <div v-if="errorMsg" class="error-message">
            {{ errorMsg }}
          </div>
          
          <div class="tab-buttons">
            <button type="button" 
                    :class="['tab-btn', registerType === 'email' ? 'active' : '']" 
                    @click="registerType = 'email'">
              邮箱注册
            </button>
            <button type="button" 
                    :class="['tab-btn', registerType === 'phone' ? 'active' : '']" 
                    @click="registerType = 'phone'">
              手机注册
            </button>
          </div>
          
          <div class="form-group">
            <input 
              type="text" 
              v-model="username" 
              placeholder="用户名"
              required
            >
            <div class="input-icon left">
              <i class="fas fa-user"></i>
            </div>
          </div>
          
          <div class="form-group" v-if="registerType === 'email'">
            <input 
              type="email" 
              v-model="email" 
              placeholder="邮箱"
              required
            >
            <div class="input-icon left">
              <i class="fas fa-envelope"></i>
            </div>
          </div>
          
          <div class="form-group" v-if="registerType === 'phone'">
            <input 
              type="tel" 
              v-model="phone" 
              placeholder="手机号"
              required
            >
            <div class="input-icon left">
              <i class="fas fa-phone"></i>
            </div>
          </div>
          
          <div class="form-group">
            <input 
              :type="showPassword ? 'text' : 'password'" 
              v-model="password" 
              placeholder="密码"
              required
            >
            <div class="input-icon left">
              <i class="fas fa-lock"></i>
            </div>
            <div class="input-icon right" @click="togglePassword">
              <i :class="showPassword ? 'fas fa-eye-slash' : 'fas fa-eye'"></i>
            </div>
          </div>
          
          <div class="form-group">
            <input 
              :type="showPassword ? 'text' : 'password'" 
              v-model="confirmPassword" 
              placeholder="确认密码"
              required
            >
            <div class="input-icon left">
              <i class="fas fa-lock"></i>
            </div>
          </div>
          
          <div class="agreement">
            <label>
              <input type="checkbox" v-model="agreeTerms" required>
              <span>我已阅读并同意 <a href="#">服务条款</a> 和 <a href="#">隐私政策</a></span>
            </label>
          </div>
          
          <button type="submit" class="register-btn" :disabled="isLoading || !agreeTerms">
            <span v-if="!isLoading">注册</span>
            <span v-else>注册中...</span>
            <i class="fas fa-arrow-right"></i>
          </button>
        </form>
        
        <div class="register-footer">
          <p>已有账号？</p>
          <a href="#" @click.prevent="goToLogin" class="login-link">
            立即登录
          </a>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const registerType = ref('email') // 默认邮箱注册
const username = ref('')
const email = ref('')
const phone = ref('')
const password = ref('')
const confirmPassword = ref('')
const agreeTerms = ref(false)
const showPassword = ref(false)
const errorMsg = ref('')
const isLoading = ref(false)

const togglePassword = () => {
  showPassword.value = !showPassword.value
}

const validateForm = () => {
  if (!username.value) {
    errorMsg.value = '请输入用户名'
    return false
  }
  
  if (registerType.value === 'email' && !email.value) {
    errorMsg.value = '请输入邮箱'
    return false
  }
  
  if (registerType.value === 'phone' && !phone.value) {
    errorMsg.value = '请输入手机号'
    return false
  }
  
  if (!password.value) {
    errorMsg.value = '请输入密码'
    return false
  }
  
  if (password.value.length < 6) {
    errorMsg.value = '密码长度不能少于6位'
    return false
  }
  
  if (password.value !== confirmPassword.value) {
    errorMsg.value = '两次输入的密码不一致'
    return false
  }
  
  if (!agreeTerms.value) {
    errorMsg.value = '请阅读并同意服务条款和隐私政策'
    return false
  }
  
  return true
}

const handleRegister = async () => {
  if (!validateForm()) return
  
  errorMsg.value = ''
  isLoading.value = true
  
  try {
    const userData = {
      username: username.value,
      password: password.value,
      email: registerType.value === 'email' ? email.value : null,
      phone: registerType.value === 'phone' ? phone.value : null
    }
    
    const result = await userStore.register(userData)
    
    if (result.success) {
      // 注册成功，跳转到首页
      router.push('/')
    } else {
      // 注册失败，显示错误信息
      errorMsg.value = result.error || '注册失败，请稍后重试'
    }
  } catch (error) {
    console.error('Registration error:', error)
    errorMsg.value = '注册过程中发生错误，请稍后重试'
  } finally {
    isLoading.value = false
  }
}

const goToLogin = () => {
  router.push('/login')
}
</script>

<style scoped>
.register-page {
  width: 100%;
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
}

.register-container {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 500px;
  display: flex;
  justify-content: center;
}

.register-box {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border-radius: 20px;
  padding: 40px;
  width: 100%;
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.2);
  transition: transform 0.3s, box-shadow 0.3s;
}

.register-box:hover {
  transform: translateY(-5px);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3);
}

.register-header {
  text-align: center;
  margin-bottom: 30px;
}

.register-header h1 {
  font-size: 1.8rem;
  color: #4158d0;
  margin-bottom: 10px;
  font-weight: 700;
}

.register-header h2 {
  font-size: 1.5rem;
  color: #333;
  margin-bottom: 10px;
}

.register-header p {
  color: #666;
  font-size: 0.9rem;
}

.tab-buttons {
  display: flex;
  margin-bottom: 20px;
  border-radius: 8px;
  overflow: hidden;
  background: #f1f1f1;
}

.tab-btn {
  flex: 1;
  padding: 12px;
  background: none;
  border: none;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
  color: #666;
}

.tab-btn.active {
  background: linear-gradient(to right, #4158d0, #c850c0);
  color: white;
}

.register-form {
  margin-bottom: 25px;
}

.form-group {
  position: relative;
  margin-bottom: 20px;
}

input[type="text"],
input[type="email"],
input[type="password"],
input[type="tel"] {
  width: 100%;
  height: 50px;
  padding: 0 20px;
  font-size: 16px;
  color: #333;
  border: 1px solid #ddd;
  border-radius: 25px;
  background: rgba(255, 255, 255, 0.8);
  box-sizing: border-box;
  text-align: center;
}

input:focus {
  outline: none;
  border-color: #4158d0;
  box-shadow: 0 0 0 2px rgba(65, 88, 208, 0.2);
}

.input-icon {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  color: #aaa;
  font-size: 16px;
}

.input-icon.left {
  left: 15px;
}

.input-icon.right {
  right: 15px;
  cursor: pointer;
}

.agreement {
  margin-bottom: 20px;
  font-size: 0.9rem;
  color: #666;
}

.agreement label {
  display: flex;
  align-items: flex-start;
  cursor: pointer;
}

.agreement input {
  margin-right: 8px;
  margin-top: 3px;
}

.agreement a {
  color: #4158d0;
  text-decoration: none;
}

.agreement a:hover {
  text-decoration: underline;
}

.register-btn {
  width: 100%;
  padding: 15px;
  background: linear-gradient(to right, #4158d0, #c850c0);
  color: white;
  border: none;
  border-radius: 30px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
  overflow: hidden;
}

.register-btn span {
  z-index: 1;
  transition: all 0.3s;
}

.register-btn i {
  margin-left: 8px;
  opacity: 0;
  transform: translateX(-10px);
  transition: all 0.3s;
  z-index: 1;
}

.register-btn:hover {
  background: linear-gradient(to right, #3a4dc0, #b040b0);
  box-shadow: 0 5px 15px rgba(65, 88, 208, 0.4);
}

.register-btn:hover span {
  transform: translateX(-5px);
}

.register-btn:hover i {
  opacity: 1;
  transform: translateX(0);
}

.register-btn:disabled {
  background: linear-gradient(to right, #91a7e0, #d9a6d9);
  cursor: not-allowed;
}

.register-btn:before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(to right, #c850c0, #ffcc70);
  opacity: 0;
  transition: all 0.5s;
}

.register-btn:hover:before {
  opacity: 1;
}

.register-footer {
  text-align: center;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #eee;
  color: #666;
  font-size: 0.9rem;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
}

.login-link {
  color: #c850c0;
  font-weight: 600;
  text-decoration: none;
  transition: color 0.3s;
}

.login-link:hover {
  color: #4158d0;
  text-decoration: underline;
}

.error-message {
  background-color: rgba(245, 108, 108, 0.1);
  color: #f56c6c;
  padding: 10px;
  border-radius: 4px;
  margin-bottom: 15px;
  font-size: 0.9rem;
  text-align: center;
}

/* 响应式调整 */
@media (max-width: 480px) {
  .register-box {
    padding: 30px 20px;
  }
  
  .register-header h1 {
    font-size: 1.5rem;
  }
  
  .register-header h2 {
    font-size: 1.3rem;
  }
}
</style> 