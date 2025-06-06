<!--
  商品发布页面
  包含发布新商品和管理已发布商品功能
-->
<template>
  <div class="product-publish-page">
    <!-- 导航栏 -->
    <NavBar />
    
    <!-- 登录状态检查 -->
    <LoginStatusChecker />
    
    <div class="container">
      <div class="section-header">
        <h2 class="section-title">{{ isEditing ? '编辑商品' : '发布二手商品' }}</h2>
        <div class="header-actions">
          <button 
            v-if="activeTab === 'my-products'" 
            class="btn-primary" 
            @click="switchToPublish"
          >
            <i class="fas fa-plus"></i> 发布新商品
          </button>
          <button 
            v-else 
            class="btn-secondary" 
            @click="switchToManage"
          >
            <i class="fas fa-list"></i> 管理我的商品
          </button>
        </div>
      </div>
      
      <div class="content-wrapper">
        <!-- 发布或编辑商品表单 -->
        <div v-if="activeTab === 'publish'" class="publish-form-container">
          <div class="publish-layout">
            <form @submit.prevent="submitProduct" class="publish-form">
              <!-- 商品基本信息 -->
              <div class="form-section">
                <h3 class="form-section-title">基本信息</h3>
                
                <div class="form-row">
                  <div class="form-group">
                    <label>商品名称 <span class="required">*</span></label>
                    <input 
                      type="text" 
                      v-model="productForm.name" 
                      required 
                      placeholder="请输入商品名称"
                    />
                  </div>
                </div>
                
                <div class="form-row">
                  <div class="form-group">
                    <label>商品分类 <span class="required">*</span></label>
                    <select v-model="productForm.categoryId" required>
                      <option value="" disabled>请选择商品分类</option>
                      <option 
                        v-for="category in productStore.getCategoryList" 
                        :key="category.id" 
                        :value="category.id"
                      >
                        {{ category.name }}
                      </option>
                    </select>
                  </div>
                  
                  <div class="form-group">
                    <label>商品价格 <span class="required">*</span></label>
                    <div class="price-input">
                      <span class="currency">¥</span>
                      <input 
                        type="number" 
                        v-model="productForm.price" 
                        step="0.01" 
                        min="0" 
                        required 
                        placeholder="0.00"
                      />
                    </div>
                  </div>
                </div>
                
                <div class="form-row">
                  <div class="form-group">
                    <label>原价</label>
                    <div class="price-input">
                      <span class="currency">¥</span>
                      <input 
                        type="number" 
                        v-model="productForm.originalPrice" 
                        step="0.01" 
                        min="0" 
                        placeholder="0.00"
                      />
                    </div>
                  </div>
                  
                  <div class="form-group">
                    <label>库存数量 <span class="required">*</span></label>
                    <input 
                      type="number" 
                      v-model="productForm.stock" 
                      min="1" 
                      required 
                      placeholder="1"
                    />
                  </div>
                </div>
              </div>
              
              <!-- 商品图片 -->
              <div class="form-section">
                <h3 class="form-section-title">商品图片</h3>
                <p class="form-section-desc">添加商品图片，第一张图片将作为主图（最多上传5张图片）</p>
                
                <div class="image-uploader">
                  <div 
                    v-for="(image, index) in productForm.images" 
                    :key="index" 
                    class="image-item"
                  >
                    <img :src="image" :alt="`商品图片${index + 1}`" />
                    <div class="image-item-actions">
                      <button type="button" class="btn-image-view" @click="previewImage(image)">
                        <i class="fas fa-eye"></i>
                      </button>
                      <button type="button" class="btn-image-delete" @click="removeImage(index)">
                        <i class="fas fa-trash"></i>
                      </button>
                    </div>
                    <div class="image-item-badge" v-if="index === 0">主图</div>
                  </div>
                  
                  <div 
                    v-if="productForm.images.length < 5" 
                    class="image-upload-trigger"
                    @click="triggerImageUpload"
                  >
                    <input 
                      type="file" 
                      ref="imageInput" 
                      accept="image/*" 
                      style="display: none;" 
                      @change="handleImageUpload" 
                    />
                    <i class="fas fa-plus"></i>
                    <span>添加图片</span>
                  </div>
                </div>
              </div>
              
              <!-- 商品描述 -->
              <div class="form-section">
                <h3 class="form-section-title">商品详情</h3>
                
                <div class="form-row">
                  <div class="form-group full-width">
                    <label>商品描述 <span class="required">*</span></label>
                    <textarea 
                      v-model="productForm.description" 
                      rows="5" 
                      required 
                      placeholder="请详细描述商品的情况，如新旧程度、使用感受等"
                    ></textarea>
                  </div>
                </div>
                
                <div class="form-row">
                  <div class="form-group">
                    <label>成色</label>
                    <select v-model="productForm.condition">
                      <option value="全新">全新</option>
                      <option value="9成新">9成新</option>
                      <option value="8成新">8成新</option>
                      <option value="7成新">7成新</option>
                      <option value="6成新及以下">6成新及以下</option>
                    </select>
                  </div>
                  
                  <div class="form-group">
                    <label>使用时长</label>
                    <input type="text" v-model="productForm.usedDuration" placeholder="如: 3个月" />
                  </div>
                </div>
                
                <div class="form-row">
                  <div class="form-group">
                    <label>品牌</label>
                    <input type="text" v-model="productForm.brand" placeholder="商品的品牌" />
                  </div>
                  
                  <div class="form-group">
                    <label>购买日期</label>
                    <input type="date" v-model="productForm.purchaseDate" />
                  </div>
                </div>
              </div>
              
              <!-- 发货和交易信息 -->
              <div class="form-section">
                <h3 class="form-section-title">交易信息</h3>
                
                <div class="form-row">
                  <div class="form-group">
                    <label>交易方式</label>
                    <div class="checkbox-group">
                      <label class="checkbox-label">
                        <input type="checkbox" v-model="productForm.supportMethods.faceToFace" />
                        <span>线下面交</span>
                      </label>
                      <label class="checkbox-label">
                        <input type="checkbox" v-model="productForm.supportMethods.delivery" />
                        <span>快递发货</span>
                      </label>
                    </div>
                  </div>
                </div>
                
                <div class="form-row" v-if="productForm.supportMethods.faceToFace">
                  <div class="form-group full-width">
                    <label>可面交地点</label>
                    <input 
                      type="text" 
                      v-model="productForm.faceToFaceLocation" 
                      placeholder="如: 大学生活区、图书馆、学校南门等" 
                    />
                  </div>
                </div>
              </div>
              
              <!-- 表单操作按钮 -->
              <div class="form-actions">
                <button type="button" class="btn-cancel" @click="cancelEdit">取消</button>
                <button type="submit" class="btn-submit">
                  {{ isEditing ? '保存修改' : '发布商品' }}
                </button>
              </div>
            </form>
            
            <!-- 商品预览卡片 -->
            <div class="product-preview-container" v-if="productForm.name || productForm.images.length > 0">
              <h3 class="preview-title">商品预览</h3>
              <div class="product-preview-card">
                <div class="preview-image">
                  <img 
                    :src="productForm.images.length > 0 ? productForm.images[0] : 'https://via.placeholder.com/300x300?text=商品预览'" 
                    alt="商品预览图"
                  />
                </div>
                <div class="preview-content">
                  <h3 class="preview-name">{{ productForm.name || '商品名称' }}</h3>
                  <div class="preview-price">¥{{ productForm.price || '0.00' }}</div>
                  <div class="preview-category" v-if="productForm.categoryId">
                    分类：{{ getCategoryName(productForm.categoryId) }}
                  </div>
                  <div class="preview-condition">
                    成色：{{ productForm.condition }}
                  </div>
                  <div class="preview-description">
                    {{ truncateDescription(productForm.description || '暂无描述') }}
                  </div>
                  <div class="preview-trade-methods">
                    <span v-if="productForm.supportMethods.delivery" class="trade-badge">
                      <i class="fas fa-truck"></i> 快递发货
                    </span>
                    <span v-if="productForm.supportMethods.faceToFace" class="trade-badge">
                      <i class="fas fa-handshake"></i> 面交
                    </span>
                  </div>
                  <button type="button" class="btn-preview" @click="openFullPreview">
                    <i class="fas fa-external-link-alt"></i> 完整预览
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 我的商品列表 -->
        <div v-else-if="activeTab === 'my-products'" class="my-products-container">
          <!-- 筛选工具栏 -->
          <div class="filter-toolbar">
            <div class="search-box">
              <input 
                type="text" 
                v-model="productSearch" 
                placeholder="搜索我的商品..." 
              />
              <i class="fas fa-search"></i>
            </div>
            <div class="filter-group">
              <select v-model="statusFilter">
                <option value="all">全部状态</option>
                <option value="online">已上架</option>
                <option value="offline">已下架</option>
              </select>
            </div>
          </div>
          
          <!-- 商品列表 -->
          <div v-if="filteredMyProducts.length > 0" class="product-list">
            <div v-for="product in filteredMyProducts" :key="product.id" class="product-card">
              <div class="product-card-image">
                <img :src="product.images && product.images.length ? product.images[0] : 'https://via.placeholder.com/150'" :alt="product.name" />
              </div>
              <div class="product-card-content">
                <h3 class="product-card-title">{{ product.name }}</h3>
                <div class="product-card-price">¥{{ product.price.toFixed(2) }}</div>
                <div class="product-card-meta">
                  <span class="meta-item">
                    <i class="fas fa-eye"></i> {{ product.views || 0 }}
                  </span>
                  <span class="meta-item">
                    <i class="fas fa-shopping-cart"></i> {{ product.sales || 0 }}
                  </span>
                </div>
                <div class="product-card-status">
                  <span :class="['status-badge', product.status === 'online' ? 'status-online' : 'status-offline']">
                    {{ product.status === 'online' ? '已上架' : '已下架' }}
                  </span>
                  <span class="publish-time">{{ formatDate(product.publishTime) }}</span>
                </div>
              </div>
              <div class="product-card-actions">
                <button class="btn-icon" title="预览" @click="previewProduct(product.id)">
                  <i class="fas fa-eye"></i>
                </button>
                <button class="btn-icon" title="编辑" @click="editProduct(product)">
                  <i class="fas fa-edit"></i>
                </button>
                <button 
                  class="btn-icon" 
                  :title="product.status === 'online' ? '下架' : '上架'"
                  @click="toggleProductStatus(product)"
                >
                  <i :class="product.status === 'online' ? 'fas fa-arrow-down' : 'fas fa-arrow-up'"></i>
                </button>
                <button class="btn-icon btn-danger" title="删除" @click="deleteProduct(product.id)">
                  <i class="fas fa-trash"></i>
                </button>
              </div>
            </div>
          </div>
          
          <div v-else class="empty-products">
            <i class="fas fa-box-open"></i>
            <p>您还没有发布商品</p>
            <button class="btn-primary" @click="switchToPublish">立即发布</button>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 图片预览模态框 -->
    <div class="image-preview-modal" v-if="imagePreview.visible" @click="closeImagePreview">
      <div class="image-preview-content" @click.stop>
        <img :src="imagePreview.src" alt="图片预览" />
        <button class="image-preview-close" @click="closeImagePreview">
          <i class="fas fa-times"></i>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/stores/user';
import { useProductStore } from '@/stores/product';
import NavBar from '@/components/NavBar.vue';
import LoginStatusChecker from '@/components/LoginStatusChecker.vue';

const router = useRouter();
const userStore = useUserStore();
const productStore = useProductStore();
const imageInput = ref(null);

// 页面状态管理
const activeTab = ref('publish'); // 'publish' or 'my-products'
const isEditing = ref(false);
const editingProductId = ref(null);

// 图片预览状态
const imagePreview = reactive({
  visible: false,
  src: ''
});

// 商品表单数据
const productForm = reactive({
  name: '',
  categoryId: '',
  category_id: '',  // 兼容数据库字段名
  price: '',
  originalPrice: '',
  original_price: '',  // 兼容数据库字段名
  stock: 1,
  description: '',
  images: [],
  condition: '9成新',
  usedDuration: '',
  used_duration: '',  // 兼容数据库字段名
  brand: '',
  purchaseDate: '',
  purchase_date: '',  // 兼容数据库字段名
  supportMethods: {
    faceToFace: true,
    delivery: true
  },
  face_to_face: 1,  // 兼容数据库字段名
  delivery: 1,  // 兼容数据库字段名
  faceToFaceLocation: '',
  face_to_face_location: ''  // 兼容数据库字段名
});

// 筛选和搜索
const productSearch = ref('');
const statusFilter = ref('all');

// 检查用户是否登录
onMounted(() => {
  if (!userStore.isLoggedIn) {
    router.push('/login');
    return;
  }

  // 加载用户商品列表
  loadMyProducts();
});

// 加载用户的商品
async function loadMyProducts() {
  // 调用productStore中的fetchUserProducts方法
  const result = await productStore.fetchUserProducts();
  if (!result.success) {
    alert('加载商品列表失败，请重试');
  }
}

// 过滤后的用户商品列表
const filteredMyProducts = computed(() => {
  let products = productStore.getUserProducts(userStore.currentUser?.id) || [];
  
  // 按状态筛选
  if (statusFilter.value !== 'all') {
    products = products.filter(p => p.status === statusFilter.value);
  }
  
  // 按名称搜索
  if (productSearch.value) {
    const searchLower = productSearch.value.toLowerCase();
    products = products.filter(p => 
      p.name.toLowerCase().includes(searchLower) || 
      p.description.toLowerCase().includes(searchLower)
    );
  }
  
  return products;
});

// 切换到发布商品
function switchToPublish() {
  activeTab.value = 'publish';
  resetProductForm();
  isEditing.value = false;
  editingProductId.value = null;
}

// 切换到管理商品
function switchToManage() {
  activeTab.value = 'my-products';
}

// 触发图片上传
function triggerImageUpload() {
  imageInput.value.click();
}

// 处理图片上传
function handleImageUpload(event) {
  const file = event.target.files[0];
  if (!file) return;
  
  // 检查文件类型
  if (!file.type.startsWith('image/')) {
    alert('请选择图片文件');
    return;
  }
  
  // 检查文件大小（限制为5MB）
  if (file.size > 5 * 1024 * 1024) {
    alert('图片大小不能超过5MB');
    return;
  }
  
  const reader = new FileReader();
  reader.onload = (e) => {
    // 使用Base64格式直接存储图片
    productForm.images.push(e.target.result);
  };
  reader.readAsDataURL(file);
  
  // 重置input，以便能够选择同一文件
  event.target.value = '';
}

// 移除图片
function removeImage(index) {
  productForm.images.splice(index, 1);
}

// 提交商品表单
async function submitProduct() {
  if (!validateProductForm()) return;
  
  try {
    // 检查用户是否登录
    if (!userStore.isLoggedIn || !userStore.token) {
      alert('您需要先登录才能发布商品');
      router.push('/login');
      return;
    }
    
    // 同步相关字段的值
    syncFormFields();
    
    const productData = {
      ...productForm,
      sellerId: userStore.currentUser.id,
      seller_id: userStore.currentUser.id,
      seller: userStore.currentUser.username,
      sellerAvatar: userStore.userAvatar
    };
    
    let result;
    
    if (isEditing.value && editingProductId.value) {
      // 更新商品
      result = await productStore.updateProduct(editingProductId.value, productData);
    } else {
      // 新增商品
      result = await productStore.addProduct(productData);
    }
    
    if (result.success) {
      alert(isEditing.value ? '商品更新成功' : '商品发布成功');
      switchToManage();
    } else {
      alert(result.error || '操作失败，请重试');
    }
  } catch (error) {
    console.error('商品提交失败:', error);
    alert('操作失败，请重试');
  }
}

// 同步表单字段（确保驼峰命名和下划线命名的字段保持一致）
function syncFormFields() {
  // 分类ID
  productForm.category_id = productForm.categoryId;
  
  // 原价
  productForm.original_price = productForm.originalPrice;
  
  // 使用时长
  productForm.used_duration = productForm.usedDuration;
  
  // 购买日期
  productForm.purchase_date = productForm.purchaseDate;
  
  // 交易方式
  productForm.face_to_face = productForm.supportMethods.faceToFace ? 1 : 0;
  productForm.delivery = productForm.supportMethods.delivery ? 1 : 0;
  
  // 交易地点
  productForm.face_to_face_location = productForm.faceToFaceLocation;
}

// 取消编辑
function cancelEdit() {
  if (isEditing.value) {
    const confirmed = confirm('确定要取消编辑吗？所有未保存的修改将丢失。');
    if (!confirmed) return;
  }
  
  if (activeTab.value === 'publish') {
    resetProductForm();
    isEditing.value = false;
    editingProductId.value = null;
  } else {
    switchToManage();
  }
}

// 重置商品表单
function resetProductForm() {
  Object.assign(productForm, {
    name: '',
    categoryId: '',
    category_id: '',
    price: '',
    originalPrice: '',
    original_price: '',
    stock: 1,
    description: '',
    images: [],
    condition: '9成新',
    usedDuration: '',
    used_duration: '',
    brand: '',
    purchaseDate: '',
    purchase_date: '',
    supportMethods: {
      faceToFace: true,
      delivery: true
    },
    face_to_face: 1,
    delivery: 1,
    faceToFaceLocation: '',
    face_to_face_location: ''
  });
}

// 验证商品表单
function validateProductForm() {
  if (!productForm.name.trim()) {
    alert('请输入商品名称');
    return false;
  }
  
  if (!productForm.categoryId) {
    alert('请选择商品分类');
    return false;
  }
  
  if (!productForm.price || Number(productForm.price) <= 0) {
    alert('请输入有效的商品价格');
    return false;
  }
  
  if (!productForm.stock || Number(productForm.stock) <= 0) {
    alert('库存数量必须大于0');
    return false;
  }
  
  if (!productForm.description.trim()) {
    alert('请输入商品描述');
    return false;
  }
  
  if (productForm.images.length === 0) {
    alert('请至少上传一张商品图片');
    return false;
  }
  
  return true;
}

// 预览商品
function previewProduct(productId) {
  router.push(`/product/${productId}`);
}

// 编辑商品
function editProduct(product) {
  isEditing.value = true;
  editingProductId.value = product.id;
  activeTab.value = 'publish';
  
  // 填充表单数据
  Object.assign(productForm, {
    name: product.name,
    categoryId: product.categoryId || product.category_id || '',
    category_id: product.category_id || product.categoryId || '',
    price: product.price,
    originalPrice: product.originalPrice || product.original_price || '',
    original_price: product.original_price || product.originalPrice || '',
    stock: product.stock,
    description: product.description || '',
    images: product.images || [],
    condition: product.condition || '9成新',
    usedDuration: product.usedDuration || product.used_duration || '',
    used_duration: product.used_duration || product.usedDuration || '',
    brand: product.brand || '',
    purchaseDate: product.purchaseDate || product.purchase_date || '',
    purchase_date: product.purchase_date || product.purchaseDate || '',
    supportMethods: product.supportMethods || {
      faceToFace: !!product.face_to_face,
      delivery: !!product.delivery
    },
    face_to_face: product.face_to_face || (product.supportMethods?.faceToFace ? 1 : 0),
    delivery: product.delivery || (product.supportMethods?.delivery ? 1 : 0),
    faceToFaceLocation: product.faceToFaceLocation || product.face_to_face_location || '',
    face_to_face_location: product.face_to_face_location || product.faceToFaceLocation || ''
  });
}

// 切换商品上下架状态
async function toggleProductStatus(product) {
  try {
    let result;
    
    if (product.status === 'online') {
      result = await productStore.unpublishProduct(product.id);
    } else {
      result = await productStore.publishProduct(product.id);
    }
    
    if (result.success) {
      alert(product.status === 'online' ? '商品已下架' : '商品已上架');
    } else {
      alert(result.error || '操作失败，请重试');
    }
  } catch (error) {
    console.error('切换商品状态失败:', error);
    alert('操作失败，请重试');
  }
}

// 删除商品
async function deleteProduct(productId) {
  const confirmed = confirm('确定要删除此商品吗？此操作不可恢复。');
  if (!confirmed) return;
  
  try {
    const result = await productStore.deleteProduct(productId);
    
    if (result.success) {
      alert('商品已删除');
    } else {
      alert(result.error || '删除失败，请重试');
    }
  } catch (error) {
    console.error('删除商品失败:', error);
    alert('删除失败，请重试');
  }
}

// 格式化日期
function formatDate(dateString) {
  if (!dateString) return '';
  
  const date = new Date(dateString);
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  });
}

// 预览图片
function previewImage(image) {
  imagePreview.src = image;
  imagePreview.visible = true;
}

// 关闭图片预览
function closeImagePreview() {
  imagePreview.visible = false;
}

// 打开完整预览
function openFullPreview() {
  // 创建临时商品对象用于预览
  const previewProduct = {
    id: 'preview',
    name: productForm.name || '商品名称预览',
    price: productForm.price || 0,
    originalPrice: productForm.originalPrice || 0,
    description: productForm.description || '暂无描述',
    images: [...productForm.images],
    categoryId: productForm.categoryId,
    condition: productForm.condition,
    brand: productForm.brand || '',
    usedDuration: productForm.usedDuration || '',
    supportMethods: {
      faceToFace: productForm.supportMethods.faceToFace,
      delivery: productForm.supportMethods.delivery
    },
    faceToFaceLocation: productForm.faceToFaceLocation || '',
    status: 'online'
  };
  
  // 先清空localStorage中的预览数据，再存储新的预览商品数据
  localStorage.removeItem('previewProduct');
  localStorage.setItem('previewProduct', JSON.stringify(previewProduct));
  
  // 打开新窗口进行预览
  window.open('/product/preview', '_blank');
}

// 获取分类名称
function getCategoryName(categoryId) {
  const category = productStore.getCategoryList.find(c => c.id === categoryId);
  return category ? category.name : '未知分类';
}

// 截断商品描述
function truncateDescription(description) {
  if (description.length > 100) {
    return description.slice(0, 100) + '...';
  }
  return description;
}
</script>

<style scoped>
.product-publish-page {
  min-height: 100vh;
}

/* 内容容器 - 使用全局背景，调整透明度 */
.container {
  position: relative;
  z-index: 1;
  background-color: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border-radius: 20px;
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.2);
  margin: 2rem auto;
  padding: 2rem;
  max-width: 1200px;
}

/* 页面头部 */
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
}

.section-title {
  font-size: 1.5rem;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 10px;
}

/* 内容包装器 */
.content-wrapper {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  padding: 25px;
}

/* 表单样式 */
.form-section {
  margin-bottom: 30px;
  padding-bottom: 25px;
  border-bottom: 1px solid #ebeef5;
}

.form-section:last-child {
  border-bottom: none;
  margin-bottom: 0;
  padding-bottom: 0;
}

.form-section-title {
  font-size: 1.2rem;
  color: #303133;
  margin-bottom: 20px;
}

.form-section-desc {
  color: #909399;
  margin-top: -15px;
  margin-bottom: 15px;
  font-size: 0.9rem;
}

.form-row {
  display: flex;
  gap: 20px;
  margin-bottom: 15px;
}

.form-group {
  flex: 1;
}

.form-group.full-width {
  flex: 0 0 100%;
}

label {
  display: block;
  margin-bottom: 8px;
  color: #606266;
  font-weight: 500;
}

.required {
  color: #f56c6c;
  margin-left: 2px;
}

input[type="text"],
input[type="number"],
input[type="date"],
select,
textarea {
  width: 100%;
  padding: 10px 15px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 14px;
  color: #606266;
  background-color: #fff;
  transition: all 0.2s;
}

input:focus,
select:focus,
textarea:focus {
  outline: none;
  border-color: #409EFF;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

.price-input {
  position: relative;
}

.price-input input {
  padding-left: 30px;
}

.currency {
  position: absolute;
  left: 10px;
  top: 50%;
  transform: translateY(-50%);
  color: #606266;
}

.checkbox-group {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.checkbox-label input {
  width: 16px;
  height: 16px;
  cursor: pointer;
}

/* 图片上传器 */
.image-uploader {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  margin-top: 10px;
}

.image-item {
  width: 120px;
  height: 120px;
  border-radius: 4px;
  overflow: hidden;
  position: relative;
  border: 1px solid #ebeef5;
}

.image-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-item-actions {
  position: absolute;
  top: 0;
  right: 0;
  background: rgba(0, 0, 0, 0.5);
  border-radius: 0 0 0 4px;
}

.btn-image-delete {
  border: none;
  background: transparent;
  color: #fff;
  padding: 6px;
  cursor: pointer;
  font-size: 12px;
}

.image-upload-trigger {
  width: 120px;
  height: 120px;
  border: 1px dashed #d9d9d9;
  border-radius: 4px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  color: #8c939d;
  transition: all 0.3s;
}

.image-upload-trigger:hover {
  border-color: #409EFF;
  color: #409EFF;
}

.image-upload-trigger i {
  font-size: 28px;
  margin-bottom: 8px;
}

/* 表单操作按钮 */
.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 15px;
  margin-top: 30px;
}

.btn-submit {
  background: #409EFF;
  color: white;
  border: none;
  padding: 12px 25px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
  font-weight: 500;
  transition: all 0.3s;
}

.btn-submit:hover {
  background: #66b1ff;
}

.btn-cancel {
  background: #f5f7fa;
  color: #606266;
  border: 1px solid #dcdfe6;
  padding: 12px 25px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
  transition: all 0.3s;
}

.btn-cancel:hover {
  color: #409EFF;
  border-color: #c6e2ff;
  background: #ecf5ff;
}

/* 商品管理页面 */
.filter-toolbar {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
  align-items: center;
}

.search-box {
  position: relative;
  width: 300px;
}

.search-box input {
  width: 100%;
  padding: 10px 15px;
  padding-right: 35px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
}

.search-box i {
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
  color: #909399;
}

.filter-group select {
  padding: 10px;
  width: 150px;
}

/* 发布表单布局 */
.publish-layout {
  display: flex;
  gap: 30px;
}

.publish-form {
  flex: 1;
  max-width: 70%;
}

/* 商品预览 */
.product-preview-container {
  width: 30%;
  min-width: 300px;
  position: sticky;
  top: 20px;
}

.preview-title {
  font-size: 18px;
  margin-bottom: 15px;
  color: #606266;
  border-bottom: 1px solid #ebeef5;
  padding-bottom: 10px;
}

.product-preview-card {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.preview-image {
  height: 250px;
  overflow: hidden;
}

.preview-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.preview-content {
  padding: 15px;
}

.preview-name {
  font-size: 18px;
  margin: 0 0 10px 0;
  color: #303133;
}

.preview-price {
  font-size: 22px;
  font-weight: bold;
  color: #f56c6c;
  margin-bottom: 15px;
}

.preview-category,
.preview-condition {
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
}

.preview-description {
  font-size: 14px;
  color: #606266;
  margin: 15px 0;
  line-height: 1.5;
  max-height: 80px;
  overflow: hidden;
}

.preview-trade-methods {
  display: flex;
  gap: 10px;
  margin-bottom: 15px;
}

.trade-badge {
  display: inline-flex;
  align-items: center;
  padding: 4px 8px;
  background: #f0f9eb;
  color: #67c23a;
  border-radius: 4px;
  font-size: 12px;
}

.trade-badge i {
  margin-right: 5px;
}

.btn-preview {
  width: 100%;
  padding: 10px;
  background: #409EFF;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-preview:hover {
  background: #66b1ff;
}

/* 图片上传改进 */
.image-uploader {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  margin-top: 10px;
}

.image-item {
  width: 120px;
  height: 120px;
  border-radius: 4px;
  overflow: hidden;
  position: relative;
  border: 1px solid #ebeef5;
  transition: all 0.3s;
}

.image-item:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.image-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-item-actions {
  position: absolute;
  top: 0;
  right: 0;
  display: flex;
  opacity: 0;
  transition: opacity 0.3s;
}

.image-item:hover .image-item-actions {
  opacity: 1;
}

.btn-image-delete,
.btn-image-view {
  border: none;
  background: rgba(0, 0, 0, 0.6);
  color: #fff;
  padding: 6px;
  cursor: pointer;
  font-size: 12px;
  transition: all 0.3s;
}

.btn-image-view {
  background: rgba(64, 158, 255, 0.6);
}

.btn-image-view:hover {
  background: rgba(64, 158, 255, 0.9);
}

.btn-image-delete:hover {
  background: rgba(245, 108, 108, 0.9);
}

.image-item-badge {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(64, 158, 255, 0.8);
  color: white;
  font-size: 12px;
  text-align: center;
  padding: 2px 0;
}

.image-upload-trigger {
  width: 120px;
  height: 120px;
  border: 2px dashed #d9d9d9;
  border-radius: 4px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  color: #8c939d;
  transition: all 0.3s;
}

.image-upload-trigger:hover {
  border-color: #409EFF;
  color: #409EFF;
  transform: translateY(-3px);
}

.image-upload-trigger i {
  font-size: 28px;
  margin-bottom: 8px;
}

/* 图片预览弹窗 */
.image-preview-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.8);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 9999;
}

.image-preview-content {
  position: relative;
  max-width: 90%;
  max-height: 90%;
}

.image-preview-content img {
  max-width: 100%;
  max-height: 90vh;
  object-fit: contain;
}

.image-preview-close {
  position: absolute;
  top: -30px;
  right: 0;
  color: white;
  background: transparent;
  border: none;
  font-size: 24px;
  cursor: pointer;
}

/* 商品列表 */
.product-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.product-card {
  background: #fff;
  border-radius: 6px;
  overflow: hidden;
  border: 1px solid #ebeef5;
  transition: all 0.3s;
}

.product-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.product-card-image {
  height: 180px;
  overflow: hidden;
}

.product-card-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.product-card:hover .product-card-image img {
  transform: scale(1.05);
}

.product-card-content {
  padding: 15px;
  position: relative;
}

.product-card-title {
  font-size: 16px;
  margin: 0 0 10px 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.product-card-price {
  color: #f56c6c;
  font-weight: bold;
  margin-bottom: 10px;
}

.product-card-meta {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.meta-item {
  color: #909399;
  font-size: 13px;
}

.meta-item i {
  margin-right: 5px;
}

.product-card-status {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.status-badge {
  padding: 2px 8px;
  border-radius: 3px;
  font-size: 12px;
  font-weight: 500;
}

.status-online {
  color: #67c23a;
  background-color: #f0f9eb;
}

.status-offline {
  color: #909399;
  background-color: #f4f4f5;
}

.publish-time {
  font-size: 12px;
  color: #909399;
}

.product-card-actions {
  padding: 10px 15px;
  background-color: #f5f7fa;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.btn-icon {
  width: 32px;
  height: 32px;
  border-radius: 4px;
  border: none;
  background: #fff;
  color: #606266;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-icon:hover {
  color: #409EFF;
  background-color: #ecf5ff;
}

.btn-icon.btn-danger:hover {
  color: #f56c6c;
  background-color: #fef0f0;
}

/* 空状态 */
.empty-products {
  padding: 60px 0;
  text-align: center;
  color: #909399;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.empty-products i {
  font-size: 48px;
  margin-bottom: 15px;
  opacity: 0.5;
}

.empty-products p {
  margin-bottom: 20px;
}

.btn-primary {
  background: #409EFF;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-primary:hover {
  background: #66b1ff;
}

.btn-secondary {
  background: #f4f4f5;
  color: #606266;
  border: 1px solid #dcdfe6;
  padding: 10px 20px;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-secondary:hover {
  color: #409EFF;
  border-color: #c6e2ff;
  background-color: #ecf5ff;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }
  
  .form-row {
    flex-direction: column;
    gap: 15px;
  }
  
  .filter-toolbar {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }
  
  .search-box {
    width: 100%;
  }
  
  .filter-group select {
    width: 100%;
  }
  
  .product-list {
    grid-template-columns: repeat(auto-fill, minmax(100%, 1fr));
  }
}
</style> 