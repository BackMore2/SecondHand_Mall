<template>
  <div class="ai-customer-service">
    <!-- 聊天按钮 -->
    <button 
      class="chat-button" 
      @click="toggleChatBox" 
      :class="{ active: isOpen }"
    >
      <span v-if="!isOpen">
        <i class="fas fa-robot"></i>
        <span class="button-text">智能客服</span>
      </span>
      <i v-else class="fas fa-times"></i>
    </button>

    <!-- 聊天框 -->
    <div class="chat-box" v-show="isOpen">
      <div class="chat-header">
        <div class="chat-title">
          <i class="fas fa-robot"></i>
          <span>智能客服助手</span>
        </div>
        <button class="close-button" @click="toggleChatBox">
          <i class="fas fa-times"></i>
        </button>
      </div>
      
      <div class="chat-messages" ref="messageContainer">
        <!-- 欢迎消息 -->
        <div class="message bot">
          <div class="avatar">
            <i class="fas fa-robot"></i>
          </div>
          <div class="message-content">
            <p>您好！我是校园二手交易平台的智能客服助手，有什么可以帮您的吗？</p>
          </div>
        </div>
        
        <!-- 消息列表 -->
        <template v-for="(msg, index) in messages" :key="index">
          <div class="message" :class="msg.type">
            <div v-if="msg.type === 'bot'" class="avatar">
              <i class="fas fa-robot"></i>
            </div>
            <div v-else class="avatar user">
              <i class="fas fa-user"></i>
            </div>
            <div class="message-content">
              <p>{{ msg.content }}</p>
            </div>
          </div>
        </template>
        
        <!-- 加载动画 -->
        <div class="message bot" v-if="loading">
          <div class="avatar">
            <i class="fas fa-robot"></i>
          </div>
          <div class="message-content typing">
            <span></span>
            <span></span>
            <span></span>
          </div>
        </div>
      </div>
      
      <div class="chat-input">
        <input 
          type="text" 
          v-model="userMessage" 
          placeholder="请输入您的问题..." 
          @keyup.enter="sendMessage"
          :disabled="loading"
        />
        <button 
          class="send-button" 
          @click="sendMessage" 
          :disabled="loading || !userMessage.trim()"
        >
          <i class="fas fa-paper-plane"></i>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch, nextTick } from 'vue';
import { sendMessageToAI } from '@/api/customerService';

// 状态变量
const isOpen = ref(false);
const userMessage = ref('');
const messages = reactive([]);
const loading = ref(false);
const messageContainer = ref(null);

// 打开/关闭聊天框
function toggleChatBox() {
  isOpen.value = !isOpen.value;
  
  if (isOpen.value) {
    // 聊天框打开时，自动聚焦到输入框
    nextTick(() => {
      const inputElement = document.querySelector('.chat-input input');
      if (inputElement) {
        inputElement.focus();
      }
    });
  }
}

// 发送消息
async function sendMessage() {
  const message = userMessage.value.trim();
  if (!message || loading.value) return;
  
  // 添加用户消息到聊天记录
  messages.push({
    type: 'user',
    content: message
  });
  
  // 清空输入框并显示加载状态
  userMessage.value = '';
  loading.value = true;
  
  try {
    // 滚动到底部
    scrollToBottom();
    
    console.log('发送消息到AI客服:', message);
    
    // 调用API获取回复
    const response = await sendMessageToAI(message);
    
    console.log('AI客服响应:', response);
    
    // 处理响应
    if (response && response.data) {
      // 添加机器人回复
      messages.push({
        type: 'bot',
        content: response.data.message
      });
    } else {
      // 错误处理
      messages.push({
        type: 'bot',
        content: '抱歉，我无法理解您的问题，请换个方式提问。'
      });
    }
  } catch (error) {
    console.error('AI客服请求失败:', error);
    messages.push({
      type: 'bot',
      content: '抱歉，系统出现了一些问题，请稍后再试。'
    });
  } finally {
    loading.value = false;
    
    // 滚动到底部
    scrollToBottom();
  }
}

// 滚动聊天记录到底部
function scrollToBottom() {
  nextTick(() => {
    if (messageContainer.value) {
      messageContainer.value.scrollTop = messageContainer.value.scrollHeight;
    }
  });
}

// 监听消息变化，自动滚动到底部
watch(messages, () => {
  scrollToBottom();
});

// 为了响应式布局，监听窗口大小变化
onMounted(() => {
  window.addEventListener('resize', () => {
    if (isOpen.value) {
      scrollToBottom();
    }
  });
});
</script>

<style scoped>
.ai-customer-service {
  position: fixed;
  bottom: 20px;
  right: 20px;
  z-index: 1000;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.chat-button {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background-color: #4CAF50;
  color: white;
  border: none;
  box-shadow: 0 4px 12px rgba(0,0,0,0.2);
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  position: relative;
}

.chat-button:hover {
  transform: scale(1.05);
  box-shadow: 0 6px 16px rgba(0,0,0,0.25);
}

.chat-button.active {
  background-color: #D32F2F;
  transform: scale(1);
}

.button-text {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  border-width: 0;
}

.chat-box {
  position: absolute;
  bottom: 80px;
  right: 0;
  width: 350px;
  height: 500px;
  background-color: white;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0,0,0,0.15);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  transition: all 0.3s;
  animation: slideIn 0.3s ease-out;
  border: 1px solid #e0e0e0;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.chat-header {
  height: 60px;
  background: linear-gradient(135deg, #4CAF50, #2E7D32);
  color: white;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
}

.chat-title {
  display: flex;
  align-items: center;
}

.chat-title i {
  margin-right: 8px;
  font-size: 18px;
}

.chat-title span {
  font-size: 16px;
  font-weight: 500;
}

.close-button {
  background: transparent;
  border: none;
  color: white;
  cursor: pointer;
  font-size: 18px;
  transition: color 0.2s;
}

.close-button:hover {
  color: #f0f0f0;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  background-color: #f5f5f5;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.message {
  display: flex;
  align-items: flex-start;
  max-width: 85%;
}

.message.user {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background-color: #E8F5E9;
  color: #4CAF50;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 8px;
}

.avatar.user {
  background-color: #E3F2FD;
  color: #2196F3;
}

.message-content {
  background-color: white;
  border-radius: 16px;
  padding: 10px 14px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
  position: relative;
}

.message.bot .message-content {
  border-top-left-radius: 4px;
}

.message.user .message-content {
  background-color: #E3F2FD;
  border-top-right-radius: 4px;
}

.message-content p {
  margin: 0;
  font-size: 14px;
  line-height: 1.5;
}

.typing {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 10px 14px;
}

.typing span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background-color: #4CAF50;
  display: inline-block;
  animation: bounce 1.4s infinite ease-in-out;
}

.typing span:nth-child(1) {
  animation-delay: 0s;
}

.typing span:nth-child(2) {
  animation-delay: 0.2s;
}

.typing span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes bounce {
  0%, 80%, 100% { 
    transform: scale(0.5);
    opacity: 0.5;
  }
  40% { 
    transform: scale(1);
    opacity: 1;
  }
}

.chat-input {
  height: 60px;
  border-top: 1px solid #e0e0e0;
  display: flex;
  align-items: center;
  padding: 0 12px;
}

.chat-input input {
  flex: 1;
  height: 40px;
  border: 1px solid #e0e0e0;
  border-radius: 20px;
  padding: 0 16px;
  outline: none;
  transition: border 0.3s;
}

.chat-input input:focus {
  border-color: #4CAF50;
}

.send-button {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: #4CAF50;
  color: white;
  border: none;
  margin-left: 10px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background-color 0.2s;
}

.send-button:hover {
  background-color: #45a049;
}

.send-button:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

/* 响应式设计 */
@media (max-width: 480px) {
  .chat-box {
    width: calc(100vw - 40px);
    height: 70vh;
    bottom: 70px;
  }
  
  .chat-button {
    width: 50px;
    height: 50px;
    font-size: 20px;
  }
}
</style> 