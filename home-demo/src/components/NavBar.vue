<template>
  <!-- 顶部导航栏 -->
  <header class="site-header">
    <div class="header-container nav-glass">
      <!-- Logo区域 -->
      <div class="logo-area">
        <router-link to="/" class="logo-link">
          <span class="logo-text">校园二手交易平台</span>
        </router-link>
      </div>
      <!-- 主导航菜单 -->
      <nav class="main-nav">
        <ul class="nav-list">
          <li class="nav-item">
            <router-link to="/" class="nav-link" :class="{ active: currentPath === '/' }">首页</router-link>
          </li>
          <li class="nav-item">
            <router-link to="/cart" class="nav-link" :class="{ active: currentPath === '/cart' }">购物车</router-link>
          </li>
          <li class="nav-item">
            <router-link to="/orders" class="nav-link" :class="{ active: currentPath === '/orders' }" v-if="userStore.isLoggedIn">我的订单</router-link>
          </li>
          <li class="nav-item" v-if="userStore.isLoggedIn">
            <router-link to="/product/publish" class="nav-link highlight">发布商品</router-link>
          </li>
        </ul>
      </nav>
      <!-- 搜索栏和用户区 -->
      <div class="nav-right">
        <div class="search-box nav-search">
          <input type="text" class="search-input" placeholder="搜索商品..." v-model="searchKeyword" @keyup.enter="searchProducts" />
          <button class="search-btn" @click="searchProducts">
            <i class="fas fa-search"></i>
          </button>
        </div>
        <div class="user-area nav-user">
          <!-- 未登录状态 -->
          <div v-if="!userStore.isLoggedIn" class="auth-buttons">
            <router-link to="/login" class="btn-login">登录</router-link>
            <router-link to="/register" class="btn-register">注册</router-link>
          </div>
          
          <!-- 已登录状态 -->
          <div v-else class="user-profile">
            <span class="username">{{ userName }}</span>
            <div class="user-dropdown">
              <div class="user-avatar" @click="toggleUserMenu">
                <img v-if="userStore.userAvatar" :src="userStore.userAvatar" alt="avatar" class="avatar-img" />
                <i v-else class="fas fa-user"></i>
              </div>
              <transition name="fade-slide">
                <div class="user-dropdown-menu" v-show="isUserMenuOpen">
                  <router-link to="/user/profile" class="dropdown-item">个人中心</router-link>
                  <router-link to="/orders" class="dropdown-item">我的订单</router-link>
                  <router-link to="/product/publish" class="dropdown-item">我的发布</router-link>
                  <router-link to="/favorites" class="dropdown-item">我的收藏</router-link>
                  <router-link to="/messages" class="dropdown-item">消息中心</router-link>
                  <div class="dropdown-divider"></div>
                  <a href="javascript:void(0)" class="dropdown-item" @click="logout">退出登录</a>
                </div>
              </transition>
            </div>
          </div>
        </div>
      </div>
    </div>
  </header>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const currentPath = computed(() => route.path)
const searchKeyword = ref('')
const isUserMenuOpen = ref(false)

// 用户名显示
const userName = computed(() => {
  if (userStore.fullName) return userStore.fullName
  return userStore.currentUser?.username || '用户'
})

function toggleUserMenu() {
  isUserMenuOpen.value = !isUserMenuOpen.value
}

function searchProducts() {
  // 搜索功能实现
  console.log('搜索:', searchKeyword.value)
}

function logout() {
  // 退出登录功能
  userStore.logout()
  router.push('/login')
}

// 点击外部关闭用户菜单
const handleClickOutside = (event) => {
  const userDropdown = document.querySelector('.user-dropdown')
  if (isUserMenuOpen.value && userDropdown && !userDropdown.contains(event.target)) {
    isUserMenuOpen.value = false
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>

<style scoped>
/* 导航栏样式 */
.site-header {
  position: sticky;
  top: 0;
  z-index: 100;
  width: 100%;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.15);
}

.header-container {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  height: 64px;
  max-width: 1200px;
  margin: 0 auto;
}

.nav-glass {
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(15px);
  -webkit-backdrop-filter: blur(15px);
  border-radius: 0 0 20px 20px;
}

.logo-area {
  display: flex;
  align-items: center;
}

.logo-link {
  text-decoration: none;
  display: flex;
  align-items: center;
}

.logo-text {
  font-size: 1.3rem;
  font-weight: bold;
  color: #409EFF;
  margin-left: 10px;
}

.main-nav {
  flex: 1;
  display: flex;
  justify-content: center;
}

.nav-list {
  display: flex;
  list-style: none;
  margin: 0;
  padding: 0;
}

.nav-item {
  margin: 0 15px;
}

.nav-link {
  text-decoration: none;
  color: #606266;
  font-weight: 500;
  padding: 5px 0;
  position: relative;
  transition: all 0.3s ease;
}

.nav-link:hover, .nav-link.active {
  color: #409EFF;
}

.nav-link::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 0;
  height: 2px;
  background-color: #409EFF;
  transition: width 0.3s ease;
}

.nav-link:hover::after, .nav-link.active::after {
  width: 100%;
}

.nav-link.highlight {
  color: #fff;
  background-color: #409EFF;
  padding: 5px 15px;
  border-radius: 20px;
}

.nav-link.highlight::after {
  display: none;
}

.nav-right {
  display: flex;
  align-items: center;
}

.search-box {
  position: relative;
  margin-right: 20px;
}

.search-input {
  width: 200px;
  padding: 8px 35px 8px 15px;
  border: 1px solid #dcdfe6;
  border-radius: 20px;
  outline: none;
  transition: all 0.3s ease;
}

.search-input:focus {
  border-color: #409EFF;
  box-shadow: 0 0 5px rgba(64, 158, 255, 0.2);
}

.search-btn {
  position: absolute;
  right: 5px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  color: #606266;
  cursor: pointer;
}

.user-area {
  position: relative;
}

/* 登录注册按钮 */
.auth-buttons {
  display: flex;
  gap: 10px;
}

.btn-login, 
.btn-register {
  text-decoration: none;
  padding: 6px 15px;
  border-radius: 4px;
  font-size: 14px;
  transition: all 0.3s;
}

.btn-login {
  color: #409EFF;
  border: 1px solid #409EFF;
}

.btn-login:hover {
  background-color: #ecf5ff;
}

.btn-register {
  background-color: #409EFF;
  color: white;
  border: 1px solid transparent;
}

.btn-register:hover {
  background-color: #66b1ff;
}

.user-profile {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.username {
  margin-right: 10px;
  color: #606266;
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  overflow: hidden;
  cursor: pointer;
  background-color: #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid rgba(255, 255, 255, 0.5);
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.user-avatar i {
  font-size: 20px;
  color: #999;
}

.user-dropdown-menu {
  position: absolute;
  top: 100%;
  right: 0;
  width: 150px;
  background-color: #fff;
  border-radius: 4px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  padding: 10px 0;
  z-index: 10;
}

.dropdown-item {
  display: block;
  padding: 8px 15px;
  text-decoration: none;
  color: #606266;
  transition: background-color 0.3s ease;
}

.dropdown-item:hover {
  background-color: #f5f7fa;
  color: #409EFF;
}

.dropdown-divider {
  height: 1px;
  background-color: #ebeef5;
  margin: 5px 0;
}

.fade-slide-enter-active, .fade-slide-leave-active {
  transition: all 0.3s ease;
}

.fade-slide-enter-from, .fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}
</style> 