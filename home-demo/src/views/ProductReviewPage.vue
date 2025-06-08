<template>
  <div class="review-page">
    <NavBar />
    
    <div class="container">
      <div class="back-link">
        <router-link :to="`/product/${productId}`">
          <i class="fas fa-arrow-left"></i> 返回商品详情
        </router-link>
      </div>
      
      <div class="review-form-container">
        <h2 class="review-title">商品评价</h2>
        
        <div class="product-info" v-if="product">
          <div class="product-image">
            <img :src="product.mainImage || 'https://via.placeholder.com/100'" :alt="product.name">
          </div>
          <div class="product-details">
            <h3>{{ product.name }}</h3>
            <p class="product-price">¥{{ formatPrice(product.price) }}</p>
          </div>
        </div>
        
        <form class="review-form" @submit.prevent="submitReview">
          <!-- 评分组件 -->
          <div class="rating-section">
            <label>评分</label>
            <div class="star-rating-container">
              <div class="star-rating-text">请选择评分</div>
              <div class="star-rating">
                <label class="star-label" v-for="star in 5" :key="star">
                  <input 
                    type="radio" 
                    name="rating" 
                    :value="star" 
                    v-model="rating"
                    class="star-input"
                  >
                  <span class="star" :class="{'star-filled': star <= rating, 'star-hover': star <= hoverRating}"></span>
                </label>
              </div>
            </div>
          </div>
          
          <!-- 文字评价 -->
          <div class="review-content">
            <label for="review-text">评价内容</label>
            <textarea 
              id="review-text"
              v-model="reviewContent"
              rows="5"
              placeholder="请分享您对商品的使用体验，这将帮助其他买家做出更好的选择..."
            ></textarea>
            <div class="text-counter">{{ reviewContent.length }}/500</div>
          </div>
          
          <!-- 图片上传 -->
          <div class="image-upload-section">
            <label>上传图片 (最多5张)</label>
            <div class="upload-container">
              <!-- 上传按钮 -->
              <div class="upload-button" @click="triggerFileInput" v-if="reviewImages.length < 5">
                <i class="fas fa-camera"></i>
                <span>添加图片</span>
              </div>
              
              <!-- 预览区域 -->
              <div 
                v-for="(image, index) in reviewImages" 
                :key="index" 
                class="image-preview"
              >
                <img :src="image.url" :alt="`上传图片${index+1}`">
                <div class="remove-image" @click="removeImage(index)">
                  <i class="fas fa-times"></i>
                </div>
              </div>
              
              <!-- 隐藏的文件输入 -->
              <input 
                type="file"
                ref="fileInput"
                accept="image/*"
                style="display: none"
                @change="handleImageUpload"
                :multiple="reviewImages.length < 4"
              >
            </div>
            <div class="upload-tip">仅支持jpg、jpeg、png格式，单张图片不超过5MB</div>
          </div>
          
          <!-- 匿名选项 -->
          <div class="anonymous-option">
            <input type="checkbox" id="anonymous" v-model="isAnonymous">
            <label for="anonymous">匿名评价</label>
          </div>
          
          <!-- 提交按钮 -->
          <div class="submit-section">
            <button 
              type="submit" 
              class="submit-btn" 
              :disabled="submitting || rating === 0"
            >
              {{ submitting ? '提交中...' : '提交评价' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import NavBar from '@/components/NavBar.vue'
import * as reviewApi from '@/api/review'
import * as productApi from '@/api/product'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 获取路由参数并确保是数字类型
const productId = Number(route.params.id) || null
const orderId = Number(route.query.orderId) || null

// 表单数据
const rating = ref(0)
const hoverRating = ref(0)
const reviewContent = ref('')
const reviewImages = ref([])
const isAnonymous = ref(false)
const submitting = ref(false)
const product = ref(null)

// 监听评分变化
const ratingText = computed(() => {
  const currentRating = rating.value
  switch (currentRating) {
    case 1: return '非常差'
    case 2: return '较差'
    case 3: return '一般'
    case 4: return '不错'
    case 5: return '非常好'
    default: return '请选择评分'
  }
})

// 处理鼠标悬停
function handleStarHover(value) {
  hoverRating.value = value
}

function handleStarLeave() {
  hoverRating.value = 0
}

// 格式化价格
function formatPrice(price) {
  if (price === null || price === undefined) return '0.00'
  return Number(price).toFixed(2)
}

// 触发文件选择
function triggerFileInput() {
  if (reviewImages.value.length >= 5) return
  const fileInput = document.querySelector('input[type="file"]')
  if (fileInput) fileInput.click()
}

// 处理图片上传
function handleImageUpload(event) {
  const files = event.target.files
  if (!files || !files.length) return
  
  const maxFilesAllowed = 5 - reviewImages.value.length
  const filesToProcess = Array.from(files).slice(0, maxFilesAllowed)
  
  filesToProcess.forEach(file => {
    // 检查文件类型
    if (!['image/jpeg', 'image/jpg', 'image/png'].includes(file.type)) {
      alert('只支持jpg、jpeg、png格式的图片')
      return
    }
    
    // 检查文件大小
    if (file.size > 5 * 1024 * 1024) { // 5MB
      alert('图片大小不能超过5MB')
      return
    }
    
    // 创建文件预览
    const reader = new FileReader()
    reader.onload = (e) => {
      reviewImages.value.push({
        file: file,
        url: e.target.result
      })
    }
    reader.readAsDataURL(file)
  })
  
  // 清除文件输入，允许重新选择相同的文件
  event.target.value = ''
}

// 移除图片
function removeImage(index) {
  reviewImages.value.splice(index, 1)
}

// 获取商品信息
async function fetchProductData() {
  try {
    const response = await productApi.getProductById(productId)
    product.value = response.data || response
  } catch (error) {
    console.error('获取商品信息失败:', error)
  }
}

// 提交评价
async function submitReview() {
  if (rating.value === 0) {
    alert('请先进行评分')
    return
  }
  
  // 检查必要的ID字段
  if (!productId) {
    alert('商品ID不能为空')
    return
  }
  
  if (!orderId) {
    alert('订单ID不能为空')
    return
  }

  try {
    submitting.value = true
    
    // 处理图片上传
    let imageUrls = []
    
    // 这里简化处理，实际项目中需要实现图片上传到服务器的功能
    // 此处仅做演示，将图片 base64 存储
    if (reviewImages.value.length > 0) {
      imageUrls = reviewImages.value.map(img => img.url)
    }
    
    // 准备评价数据
    const reviewData = {
      productId: Number(productId),
      orderId: Number(orderId),
      userId: Number(userStore.userId),
      rating: rating.value,
      comment: reviewContent.value,
      images: JSON.stringify(imageUrls),
      anonymous: isAnonymous.value,
      status: 'APPROVED',  // 使用与后端一致的状态值
    }

    console.log('提交评价数据:', reviewData)
    
    // 提交评价
    await reviewApi.addReview(reviewData)
    
    alert('评价提交成功！')
    
    // 评价成功后返回订单详情页
    if (orderId) {
      router.push(`/orders/${orderId}`)
    } else {
      router.push(`/product/${productId}`)
    }
  } catch (error) {
    console.error('提交评价失败:', error)
    alert('提交评价失败: ' + (error.response?.data?.message || error.message))
  } finally {
    submitting.value = false
  }
}

// 检查必要的参数
onMounted(() => {
  if (!productId) {
    alert('商品ID不能为空')
    router.push('/home')
    return
  }
  
  if (!orderId) {
    alert('订单ID不能为空')
    router.push(`/product/${productId}`)
    return
  }
  
  fetchProductData()
})
</script>

<style scoped>
.review-page {
  min-height: 100vh;
  padding-bottom: 60px;
}

.container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
  margin-top: 20px;
}

.back-link {
  margin-bottom: 20px;
}

.back-link a {
  color: #606266;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  font-size: 14px;
}

.back-link a:hover {
  color: #409EFF;
}

.back-link i {
  margin-right: 5px;
}

.review-form-container {
  background-color: white;
  border-radius: 10px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  padding: 30px;
}

.review-title {
  text-align: center;
  margin-bottom: 30px;
  color: #303133;
}

.product-info {
  display: flex;
  align-items: center;
  margin-bottom: 30px;
  border-bottom: 1px solid #eee;
  padding-bottom: 20px;
}

.product-image {
  width: 100px;
  height: 100px;
  margin-right: 20px;
  flex-shrink: 0;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 6px;
}

.product-details h3 {
  margin: 0 0 10px 0;
  font-size: 18px;
}

.product-price {
  color: #f56c6c;
  font-size: 16px;
  font-weight: 500;
}

.review-form {
  display: flex;
  flex-direction: column;
  gap: 25px;
}

.rating-section, .review-content, .image-upload-section {
  display: flex;
  flex-direction: column;
}

label {
  font-weight: 500;
  margin-bottom: 8px;
  color: #606266;
}

.star-rating-container {
  margin-top: 10px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.star-rating-text {
  font-size: 14px;
  color: #909399;
  margin-bottom: 10px;
  text-align: center;
}

.star-rating {
  display: flex;
  justify-content: center;
  gap: 10px;
}

.star-label {
  cursor: pointer;
  position: relative;
  display: inline-block;
}

.star-input {
  position: absolute;
  opacity: 0;
  width: 0;
  height: 0;
}

.star {
  display: inline-block;
  width: 30px;
  height: 30px;
  background-image: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="%23dcdfe6" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg>');
  background-size: contain;
  background-repeat: no-repeat;
  transition: all 0.2s ease;
}

.star-filled,
.star-hover {
  background-image: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="%23f7ba2a" stroke="%23f7ba2a" stroke-width="1" stroke-linecap="round" stroke-linejoin="round"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg>');
}

.star-label:hover .star {
  transform: scale(1.2);
}

textarea {
  width: 100%;
  padding: 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  outline: none;
  transition: border 0.3s;
  font-size: 14px;
  resize: none;
}

textarea:focus {
  border-color: #409EFF;
}

.text-counter {
  text-align: right;
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.upload-container {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 8px;
}

.upload-button {
  width: 100px;
  height: 100px;
  border: 1px dashed #dcdfe6;
  border-radius: 4px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #909399;
  transition: all 0.3s;
}

.upload-button:hover {
  color: #409EFF;
  border-color: #409EFF;
}

.upload-button i {
  font-size: 24px;
  margin-bottom: 8px;
}

.image-preview {
  width: 100px;
  height: 100px;
  position: relative;
  border-radius: 4px;
  overflow: hidden;
}

.image-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.remove-image {
  position: absolute;
  top: 0;
  right: 0;
  background-color: rgba(0, 0, 0, 0.5);
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: white;
}

.upload-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}

.anonymous-option {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 10px;
}

.submit-section {
  margin-top: 10px;
  display: flex;
  justify-content: center;
}

.submit-btn {
  background-color: #409EFF;
  color: white;
  border: none;
  padding: 12px 30px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
  transition: background-color 0.3s;
}

.submit-btn:not(:disabled):hover {
  background-color: #66b1ff;
}

.submit-btn:disabled {
  background-color: #a0cfff;
  cursor: not-allowed;
}
</style> 