<!-- 登录状态检查组件 -->
<template>
  <div v-if="!isLoggedIn" class="login-checker">
    <div class="login-checker-content">
      <div class="login-checker-icon">
        <i class="fas fa-exclamation-circle"></i>
      </div>
      <h3>请先登录</h3>
      <p>您需要登录才能访问此功能</p>
      <div class="login-checker-actions">
        <button class="btn-primary" @click="goToLogin">前往登录</button>
        <button class="btn-secondary" @click="goBack">返回</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/stores/user';

const router = useRouter();
const userStore = useUserStore();

// 计算属性：用户是否已登录
const isLoggedIn = computed(() => {
  return userStore.isLoggedIn && userStore.token;
});

// 挂载时检查登录状态
onMounted(() => {
  // 输出登录状态信息（调试用）
  console.log('当前登录状态:', isLoggedIn.value);
  console.log('Token存在:', !!userStore.token);
  
  // 如果未登录，显示登录提示
  if (!isLoggedIn.value) {
    console.log('用户未登录，需要先登录');
  }
});

// 前往登录页
function goToLogin() {
  router.push('/login');
}

// 返回上一页
function goBack() {
  router.go(-1);
}
</script>

<style scoped>
.login-checker {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.7);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.login-checker-content {
  background-color: white;
  padding: 2rem;
  border-radius: 8px;
  text-align: center;
  width: 90%;
  max-width: 400px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
}

.login-checker-icon {
  font-size: 3rem;
  color: #f44336;
  margin-bottom: 1rem;
}

.login-checker-content h3 {
  margin: 0 0 0.5rem;
  font-size: 1.5rem;
}

.login-checker-content p {
  margin: 0 0 1.5rem;
  color: #666;
}

.login-checker-actions {
  display: flex;
  justify-content: center;
  gap: 1rem;
}

.btn-primary, .btn-secondary {
  padding: 0.5rem 1.5rem;
  border: none;
  border-radius: 4px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.2s;
}

.btn-primary {
  background-color: #4caf50;
  color: white;
}

.btn-secondary {
  background-color: #f5f5f5;
  color: #333;
}

.btn-primary:hover {
  background-color: #43a047;
}

.btn-secondary:hover {
  background-color: #e0e0e0;
}
</style> 