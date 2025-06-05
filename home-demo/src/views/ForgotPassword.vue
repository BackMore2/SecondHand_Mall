<template>
  <div class="forgot-password-page">
    <div class="forgot-container">
      <div class="forgot-box">
        <div class="forgot-password-header">
          <h1>找回密码</h1>
          <p>请输入您的邮箱，我们将发送重置密码链接</p>
        </div>
        
        <div v-if="!emailSent">
          <form @submit.prevent="handleSubmit" class="forgot-password-form">
            <div v-if="errorMsg" class="error-message">
              {{ errorMsg }}
            </div>
            
            <div v-if="successMsg" class="success-message">
              {{ successMsg }}
            </div>
            
            <div class="form-group">
              <input 
                type="email" 
                v-model="email" 
                placeholder="请输入您的邮箱"
                required
              >
              <div class="input-icon left">
                <i class="fas fa-envelope"></i>
              </div>
            </div>
            
            <button type="submit" class="reset-btn" :disabled="isLoading">
              <span v-if="!isLoading">发送重置链接</span>
              <span v-else>发送中...</span>
            </button>
          </form>
        </div>
        
        <div v-else class="email-sent-container">
          <div class="email-sent-icon">
            <i class="fas fa-envelope-open-text"></i>
          </div>
          <h2>邮件已发送</h2>
          <p>我们已向 {{ email }} 发送了密码重置链接，请查收邮件并按照提示操作</p>
          <p class="email-note">如果您没有收到邮件，请检查垃圾邮件文件夹</p>
        </div>
        
        <div class="forgot-password-footer">
          <a href="#" @click.prevent="goToLogin" class="back-link">
            <i class="fas fa-arrow-left"></i> 返回登录
          </a>
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

const email = ref('')
const errorMsg = ref('')
const successMsg = ref('')
const isLoading = ref(false)
const emailSent = ref(false)

const handleSubmit = async () => {
  if (!email.value) {
    errorMsg.value = '请输入邮箱地址'
    return
  }
  
  errorMsg.value = ''
  successMsg.value = ''
  isLoading.value = true
  
  try {
    const result = await userStore.requestPasswordReset(email.value)
    
    if (result.success) {
      // 发送成功
      emailSent.value = true
    } else {
      // 发送失败
      errorMsg.value = result.error || '发送重置链接失败，请稍后重试'
    }
  } catch (error) {
    console.error('Password reset request error:', error)
    errorMsg.value = '发送过程中发生错误，请稍后重试'
  } finally {
    isLoading.value = false
  }
}

const goToLogin = () => {
  router.push('/login')
}
</script>

<style scoped>
.forgot-password-page {
  width: 100%;
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
}

.forgot-container {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 450px;
  display: flex;
  justify-content: center;
}

.forgot-box {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border-radius: 20px;
  padding: 40px;
  width: 100%;
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.2);
  transition: transform 0.3s, box-shadow 0.3s;
}

.forgot-password-header {
  text-align: center;
  margin-bottom: 30px;
}

.forgot-password-header h1 {
  font-size: 1.8rem;
  color: #4158d0;
  margin-bottom: 15px;
  font-weight: 700;
}

.forgot-password-header p {
  color: #666;
  font-size: 1rem;
}

.forgot-password-form {
  margin-bottom: 25px;
}

.form-group {
  position: relative;
  margin-bottom: 25px;
}

input[type="email"] {
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

.reset-btn {
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
}

.reset-btn:hover {
  background: linear-gradient(to right, #3a4dc0, #b040b0);
  box-shadow: 0 5px 15px rgba(65, 88, 208, 0.4);
}

.reset-btn:disabled {
  background: linear-gradient(to right, #91a7e0, #d9a6d9);
  cursor: not-allowed;
}

.forgot-password-footer {
  text-align: center;
  margin-top: 25px;
}

.back-link {
  color: #4158d0;
  font-size: 0.9rem;
  text-decoration: none;
  transition: color 0.3s;
  display: inline-flex;
  align-items: center;
  gap: 5px;
}

.back-link:hover {
  color: #c850c0;
}

.back-link i {
  font-size: 0.8rem;
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

.success-message {
  background-color: rgba(103, 194, 58, 0.1);
  color: #67c23a;
  padding: 10px;
  border-radius: 4px;
  margin-bottom: 15px;
  font-size: 0.9rem;
  text-align: center;
}

.email-sent-container {
  text-align: center;
  padding: 20px 0;
}

.email-sent-icon {
  font-size: 4rem;
  color: #4158d0;
  margin-bottom: 20px;
}

.email-sent-container h2 {
  font-size: 1.5rem;
  color: #333;
  margin-bottom: 15px;
}

.email-sent-container p {
  color: #666;
  margin-bottom: 10px;
}

.email-note {
  font-size: 0.9rem;
  color: #909399;
  font-style: italic;
}

/* 响应式调整 */
@media (max-width: 480px) {
  .forgot-password-box {
    padding: 30px 20px;
  }
  
  .forgot-password-header h1 {
    font-size: 1.5rem;
  }
}
</style> 