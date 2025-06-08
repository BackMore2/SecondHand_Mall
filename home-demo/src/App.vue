<template>
  <div class="app-container">
    <div class="global-background">
      <div class="shape"></div>
      <div class="shape"></div>
    </div>
    <router-view />
    <AiCustomerService />
  </div>
</template>

<script setup>
// 不再需要手动导入 HomePage 和 CartPage
import AiCustomerService from '@/components/AiCustomerService.vue';
</script>

<style>
/* 全局样式重置 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
  font-family: 'Helvetica Neue', Arial, sans-serif;
}

html, body {
  height: 100%;
  width: 100%;
}

body {
  overflow-x: hidden;
  background-color: #f5f7fa;
  color: #303133;
}

.app-container {
  position: relative;
  min-height: 100vh;
  overflow: hidden;
}

.global-background {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #4158d0, #c850c0, #ffcc70);
  z-index: -1;
  opacity: 0.85;
}

.shape {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(5px);
  -webkit-backdrop-filter: blur(5px);
  animation: float 10s infinite ease-in-out;
}

.shape:nth-child(1) {
  width: 600px;
  height: 600px;
  top: -200px;
  left: -100px;
}

.shape:nth-child(2) {
  width: 500px;
  height: 500px;
  bottom: -150px;
  right: -100px;
  animation-delay: 3s;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0) rotate(0deg);
  }
  50% {
    transform: translateY(-30px) rotate(5deg);
  }
}

/* 内容容器，确保内容在背景上方 */
.container {
  position: relative;
  z-index: 1;
  background-color: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border-radius: 20px;
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.2);
  margin: 2rem auto;
  padding: 2rem;
  max-width: 1200px;
}
</style>