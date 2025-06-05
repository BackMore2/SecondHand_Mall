<template>
  <div class="login-page">
    <div class="login-container">
      <div class="login-box">
        <div class="login-header">
          <h1>校园二手交易平台</h1>
          <h2>欢迎回来</h2>
          <p>请登录您的账号继续访问</p>
        </div>
        
        <form @submit.prevent="handleLogin" class="login-form">
          <div v-if="errorMsg" class="error-message">
            {{ errorMsg }}
          </div>
          
          <div class="form-group">
            <input 
              type="text" 
              v-model="credential" 
              placeholder="邮箱/手机号"
              required
            >
            <div class="input-icon left">
              <i class="fas fa-user"></i>
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
          
          <div class="remember-forgot">
            <label class="remember-me">
              <input type="checkbox" v-model="rememberMe">
              <span>记住我</span>
            </label>
            <a href="#" @click.prevent="goToForgotPassword" class="forgot-link">忘记密码？</a>
          </div>
          
          <button type="submit" class="login-btn" :disabled="isLoading">
            <span v-if="!isLoading">登录</span>
            <span v-else>登录中...</span>
            <i class="fas fa-arrow-right"></i>
          </button>
        </form>
        
        <div class="login-footer">
          <p>还没有账号？</p>
          <a href="#" @click.prevent="goToRegister" class="register-link">
            立即注册
          </a>
        </div>
        
        <div class="social-login">
          <p>或使用以下方式登录</p>
          <div class="social-icons">
            <a href="#" class="social-icon wechat">
              <i class="fab fa-weixin"></i>
            </a>
            <a href="#" class="social-icon qq">
              <i class="fab fa-qq"></i>
            </a>
            <a href="#" class="social-icon weibo">
              <i class="fab fa-weibo"></i>
            </a>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const credential = ref('') // 改为 credential，可以是邮箱或手机号
const password = ref('')
const rememberMe = ref(false)
const showPassword = ref(false)
const errorMsg = ref('')
const isLoading = ref(false)

const handleLogin = async () => {
  if (!credential.value || !password.value) {
    errorMsg.value = '请输入账号和密码'
    return
  }
  
  errorMsg.value = ''
  isLoading.value = true
  
  try {
    const result = await userStore.login({
      credential: credential.value,
      password: password.value
    })
    
    if (result.success) {
      // 登录成功，跳转到首页
      router.push('/')
    } else {
      // 登录失败，显示错误信息
      errorMsg.value = result.error || '登录失败，请检查账号和密码'
    }
  } catch (error) {
    console.error('Login error:', error)
    errorMsg.value = '登录过程中发生错误，请稍后重试'
  } finally {
    isLoading.value = false
  }
}

const togglePassword = () => {
  showPassword.value = !showPassword.value
}

const goToForgotPassword = () => {
  router.push('/forgot-password')
}

const goToRegister = () => {
  router.push('/register')
}
</script>

<style scoped>
.login-page {
  width: 100%;
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
}

.login-container {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 400px;
  display: flex;
  justify-content: center;
}

.login-box {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border-radius: 20px;
  padding: 40px;
  width: 100%;
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.2);
  transition: transform 0.3s, box-shadow 0.3s;
}

.login-box:hover {
  transform: translateY(-5px);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3);
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-header h1 {
  font-size: 1.8rem;
  color: #4158d0;
  margin-bottom: 10px;
  font-weight: 700;
}

.login-header h2 {
  font-size: 1.5rem;
  color: #333;
  margin-bottom: 10px;
}

.login-header p {
  color: #666;
  font-size: 0.9rem;
}

.login-form {
  margin-bottom: 25px;
}

.form-group {
  position: relative;
  margin-bottom: 20px;
}

input[type="text"],
input[type="password"] {
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

.remember-forgot {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  font-size: 0.9rem;
}

.remember-me {
  display: flex;
  align-items: center;
  color: #666;
  cursor: pointer;
}

.remember-me input {
  width: auto;
  margin-right: 8px;
  cursor: pointer;
}

.forgot-link {
  color: #4158d0;
  text-decoration: none;
  transition: color 0.3s;
}

.forgot-link:hover {
  color: #c850c0;
  text-decoration: underline;
}

.login-btn {
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

.login-btn span {
  z-index: 1;
  transition: all 0.3s;
}

.login-btn i {
  margin-left: 8px;
  opacity: 0;
  transform: translateX(-10px);
  transition: all 0.3s;
  z-index: 1;
}

.login-btn:hover {
  background: linear-gradient(to right, #3a4dc0, #b040b0);
  box-shadow: 0 5px 15px rgba(65, 88, 208, 0.4);
}

.login-btn:hover span {
  transform: translateX(-5px);
}

.login-btn:hover i {
  opacity: 1;
  transform: translateX(0);
}

.login-btn:before {
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

.login-btn:hover:before {
  opacity: 1;
}

.login-footer {
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

.register-link {
  color: #c850c0;
  font-weight: 600;
  text-decoration: none;
  transition: color 0.3s;
}

.register-link:hover {
  color: #4158d0;
  text-decoration: underline;
}

.social-login {
  margin-top: 25px;
  text-align: center;
}

.social-login p {
  color: #666;
  font-size: 0.9rem;
  margin-bottom: 15px;
  position: relative;
}

.social-login p:before,
.social-login p:after {
  content: '';
  position: absolute;
  top: 50%;
  width: 60px;
  height: 1px;
  background: #ddd;
}

.social-login p:before {
  left: 0;
}

.social-login p:after {
  right: 0;
}

.social-icons {
  display: flex;
  justify-content: center;
  gap: 15px;
}

.social-icon {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  color: white;
  font-size: 1.2rem;
  transition: all 0.3s;
}

.social-icon:hover {
  transform: translateY(-3px);
  box-shadow: 0 5px 10px rgba(0, 0, 0, 0.2);
}

.wechat {
  background-color: #07C160;
}

.qq {
  background-color: #12B7F5;
}

.weibo {
  background-color: #E6162D;
}

/* 响应式调整 */
@media (max-width: 480px) {
  .login-box {
    padding: 30px 20px;
  }
  
  .login-header h1 {
    font-size: 1.5rem;
  }
  
  .login-header h2 {
    font-size: 1.3rem;
  }
}

/* 错误消息样式 */
.error-message {
  background-color: rgba(245, 108, 108, 0.1);
  color: #f56c6c;
  padding: 10px;
  border-radius: 4px;
  margin-bottom: 15px;
  font-size: 0.9rem;
  text-align: center;
}

/* 禁用按钮样式 */
.login-btn:disabled {
  background: linear-gradient(to right, #91a7e0, #d9a6d9);
  cursor: not-allowed;
}
</style> 