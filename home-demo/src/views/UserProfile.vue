<template>
  <div class="profile-page">
    <NavBar />
    
    <div class="container">
      <div class="section-header">
        <h2 class="section-title">个人中心</h2>
      </div>
      
      <div class="profile-container">
        <!-- 左侧导航 -->
        <div class="profile-sidebar">
          <div class="user-info">
            <div class="avatar-container">
              <img :src="userStore.userAvatar" alt="用户头像" class="user-avatar" />
              <div class="avatar-upload" @click="triggerFileUpload">
                <i class="fas fa-camera"></i>
                <input 
                  type="file" 
                  ref="fileInput" 
                  style="display:none" 
                  accept="image/*" 
                  @change="handleAvatarChange"
                />
              </div>
              <div v-if="isUploadingAvatar" class="avatar-uploading">
                <i class="fas fa-spinner fa-spin"></i>
              </div>
            </div>
            <div v-if="avatarUploadError" class="error-message avatar-error">
              <i class="fas fa-times-circle"></i> {{ avatarUploadError }}
            </div>
            <div v-if="avatarUploadSuccess" class="success-message avatar-success">
              <i class="fas fa-check-circle"></i> 头像上传成功
            </div>
            <div class="user-name">{{ userName }}</div>
          </div>
          
          <div class="nav-menu">
            <a 
              href="#" 
              @click.prevent="switchTab('profile')" 
              :class="['nav-item', activeTab === 'profile' ? 'active' : '']"
            >
              <i class="fas fa-user"></i> 个人资料
            </a>
            <a 
              href="#" 
              @click.prevent="switchTab('address')" 
              :class="['nav-item', activeTab === 'address' ? 'active' : '']"
            >
              <i class="fas fa-map-marker-alt"></i> 收货地址
            </a>
            <a 
              href="#" 
              @click.prevent="switchTab('security')" 
              :class="['nav-item', activeTab === 'security' ? 'active' : '']"
            >
              <i class="fas fa-shield-alt"></i> 安全设置
            </a>
            <a 
              href="#" 
              @click.prevent="logout" 
              class="nav-item logout"
            >
              <i class="fas fa-sign-out-alt"></i> 退出登录
            </a>
          </div>
        </div>
        
        <!-- 右侧内容区 -->
        <div class="profile-content">
          <!-- 个人资料 -->
          <div v-if="activeTab === 'profile'" class="tab-content">
            <h3 class="tab-title">个人资料</h3>
            
            <div v-if="updateProfileSuccess" class="success-message">
              <i class="fas fa-check-circle"></i> 个人资料更新成功
            </div>
            
            <div v-if="updateProfileError" class="error-message">
              <i class="fas fa-times-circle"></i> {{ updateProfileError }}
            </div>
            
            <form @submit.prevent="updateProfile" class="profile-form">
              <div class="profile-grid">
                <div class="form-group compact">
                  <label>用户名</label>
                  <input type="text" v-model="profileForm.username" />
                </div>
                
                <div class="form-group compact">
                  <label>注册时间</label>
                  <input type="text" :value="formatDate(userStore.currentUser?.createdAt)" disabled />
                </div>
              
                <div class="form-group compact">
                  <label>电子邮箱</label>
                  <input type="email" v-model="profileForm.email" />
                </div>
                
                <div class="form-group compact">
                  <label>手机号码</label>
                  <input type="tel" v-model="profileForm.phone" />
                </div>
              </div>
              
              <div class="form-actions">
                <button type="submit" class="btn-primary" :disabled="isUpdatingProfile">
                  <span v-if="!isUpdatingProfile">保存修改</span>
                  <span v-else>保存中...</span>
                </button>
              </div>
            </form>
          </div>
          
          <!-- 收货地址 -->
          <div v-if="activeTab === 'address'" class="tab-content">
            <div class="tab-header">
              <h3 class="tab-title">收货地址</h3>
              <button class="btn-add" @click="showAddressForm = true; editingAddressId = null;">
                <i class="fas fa-plus"></i> 新增地址
              </button>
            </div>
            
            <div v-if="addressOperationMsg.show" :class="['message', addressOperationMsg.type]">
              <i :class="addressOperationMsg.type === 'success' ? 'fas fa-check-circle' : 'fas fa-times-circle'"></i>
              {{ addressOperationMsg.text }}
            </div>
            
            <!-- 地址列表 -->
            <div v-if="!showAddressForm">
              <div v-if="userStore.addresses.length === 0" class="empty-data">
                <i class="fas fa-map-marker-alt"></i>
                <p>您还没有添加收货地址</p>
              </div>
              
              <div v-else class="address-list">
                <div v-for="address in userStore.addresses" :key="address.id" class="address-card">
                  <div class="address-info">
                    <div class="address-header">
                      <span class="recipient">{{ address.recipientName }}</span>
                      <span class="phone">{{ address.recipientPhone }}</span>
                      <span v-if="address.isDefault" class="default-tag">默认</span>
                    </div>
                    <div class="address-detail">
                      {{ address.address }}
                    </div>
                  </div>
                  <div class="address-actions">
                    <a href="#" @click.prevent="editAddress(address)">编辑</a>
                    <a href="#" @click.prevent="deleteAddress(address.id)">删除</a>
                    <a v-if="!address.isDefault" href="#" @click.prevent="setDefaultAddress(address.id)">设为默认</a>
                  </div>
                </div>
              </div>
            </div>
            
            <!-- 地址表单 -->
            <div v-if="showAddressForm" class="address-form-container">
              <h4>{{ editingAddressId ? '编辑地址' : '新增地址' }}</h4>
              
              <form @submit.prevent="saveAddress" class="address-form">
                <div class="form-row">
                  <div class="form-group">
                    <label>收货人</label>
                    <input type="text" v-model="addressForm.name" required />
                  </div>
                  
                  <div class="form-group">
                    <label>手机号码</label>
                    <input type="tel" v-model="addressForm.phone" required />
                  </div>
                </div>
                
                <div class="form-row">
                  <div class="form-group">
                    <label>省份</label>
                    <input type="text" v-model="addressForm.province" required />
                  </div>
                  
                  <div class="form-group">
                    <label>城市</label>
                    <input type="text" v-model="addressForm.city" required />
                  </div>
                </div>
                
                <div class="form-row">
                  <div class="form-group">
                    <label>区/县</label>
                    <input type="text" v-model="addressForm.district" required />
                  </div>
                </div>
                
                <div class="form-group">
                  <label>详细地址</label>
                  <input type="text" v-model="addressForm.address" required />
                </div>
                
                <div class="form-group checkbox-group">
                  <label class="checkbox-label">
                    <input type="checkbox" v-model="addressForm.isDefault" />
                    <span>设为默认收货地址</span>
                  </label>
                </div>
                
                <div class="form-actions">
                  <button type="button" class="btn-cancel" @click="showAddressForm = false">取消</button>
                  <button type="submit" class="btn-primary" :disabled="isProcessingAddress">
                    <span v-if="!isProcessingAddress">保存</span>
                    <span v-else>保存中...</span>
                  </button>
                </div>
              </form>
            </div>
          </div>
          
          <!-- 安全设置 -->
          <div v-if="activeTab === 'security'" class="tab-content">
            <h3 class="tab-title">安全设置</h3>
            
            <div v-if="passwordUpdateMsg.show" :class="['message', passwordUpdateMsg.type]">
              <i :class="passwordUpdateMsg.type === 'success' ? 'fas fa-check-circle' : 'fas fa-times-circle'"></i>
              {{ passwordUpdateMsg.text }}
            </div>
            
            <form @submit.prevent="changePassword" class="password-form">
              <div class="form-group">
                <label>当前密码</label>
                <div class="password-input">
                  <input 
                    :type="showOldPassword ? 'text' : 'password'" 
                    v-model="passwordForm.oldPassword" 
                    required 
                  />
                  <button 
                    type="button" 
                    class="toggle-password" 
                    @click="showOldPassword = !showOldPassword"
                  >
                    <i :class="showOldPassword ? 'fas fa-eye-slash' : 'fas fa-eye'"></i>
                  </button>
                </div>
              </div>
              
              <div class="form-group">
                <label>新密码</label>
                <div class="password-input">
                  <input 
                    :type="showNewPassword ? 'text' : 'password'" 
                    v-model="passwordForm.newPassword" 
                    required 
                  />
                  <button 
                    type="button" 
                    class="toggle-password" 
                    @click="showNewPassword = !showNewPassword"
                  >
                    <i :class="showNewPassword ? 'fas fa-eye-slash' : 'fas fa-eye'"></i>
                  </button>
                </div>
              </div>
              
              <div class="form-group">
                <label>确认新密码</label>
                <div class="password-input">
                  <input 
                    :type="showConfirmPassword ? 'text' : 'password'" 
                    v-model="passwordForm.confirmPassword" 
                    required 
                  />
                  <button 
                    type="button" 
                    class="toggle-password" 
                    @click="showConfirmPassword = !showConfirmPassword"
                  >
                    <i :class="showConfirmPassword ? 'fas fa-eye-slash' : 'fas fa-eye'"></i>
                  </button>
                </div>
              </div>
              
              <div class="password-strength" v-if="passwordForm.newPassword">
                <div class="strength-label">密码强度：</div>
                <div class="strength-meter">
                  <div 
                    class="strength-value" 
                    :style="{ width: passwordStrength.percent + '%' }" 
                    :class="passwordStrength.level"
                  ></div>
                </div>
                <div class="strength-text" :class="passwordStrength.level">{{ passwordStrength.text }}</div>
              </div>
              
              <div class="form-actions">
                <button type="submit" class="btn-primary" :disabled="isChangingPassword || !isPasswordValid">
                  <span v-if="!isChangingPassword">修改密码</span>
                  <span v-else>修改中...</span>
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import NavBar from '@/components/NavBar.vue'

const router = useRouter()
const userStore = useUserStore()
const fileInput = ref(null)
const activeTab = ref('profile')
const showAddressForm = ref(false)
const editingAddressId = ref(null)
const updateProfileSuccess = ref(false)
const updateProfileError = ref('')
const isUpdatingProfile = ref(false)
const isProcessingAddress = ref(false)

// 检查登录状态
onMounted(() => {
  if (!userStore.isLoggedIn) {
    router.push('/login')
  }
  // 不需要调用 initProfileForm，因为我们已经使用 watch 监听用户信息变化
})

// 选项卡管理
const switchTab = (tab) => {
  activeTab.value = tab
}

// 头像上传相关
const isUploadingAvatar = ref(false)
const avatarUploadError = ref('')
const avatarUploadSuccess = ref(false)

// 触发文件选择
const triggerFileUpload = () => {
  fileInput.value.click()
}

// 处理头像上传
const handleAvatarChange = async (event) => {
  const file = event.target.files[0]
  if (!file) return
  
  // 验证文件类型
  if (!file.type.startsWith('image/')) {
    avatarUploadError.value = '请选择图片文件'
    return
  }
  
  // 验证文件大小（2MB 以内）
  if (file.size > 2 * 1024 * 1024) {
    avatarUploadError.value = '图片大小不能超过 2MB'
    return
  }
  
  avatarUploadError.value = ''
  isUploadingAvatar.value = true
  
  try {
    const formData = new FormData()
    formData.append('file', file)
    
    // 调用上传 API
    const userId = userStore.currentUser.id
    console.log('上传头像，用户ID:', userId)
    
    const response = await fetch(`http://localhost:8080/api/users/${userId}/avatar`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      },
      body: formData
    })
    
    console.log('上传头像响应状态:', response.status)
    const result = await response.json()
    console.log('上传头像响应数据:', result)
    
    if (response.ok) {
      // 上传成功
      avatarUploadSuccess.value = true
      setTimeout(() => {
        avatarUploadSuccess.value = false
      }, 3000)
      
      // 更新用户信息
      userStore.currentUser = result.user
      
      // 打印头像URL，用于调试
      console.log('更新后的头像URL:', userStore.userAvatar)
      console.log('原始头像路径:', result.user.avatar)
    } else {
      // 上传失败
      avatarUploadError.value = result.error || '头像上传失败'
    }
  } catch (error) {
    console.error('Avatar upload error:', error)
    avatarUploadError.value = '头像上传过程中发生错误'
  } finally {
    isUploadingAvatar.value = false
    // 清空文件输入以便下次选择同一文件时触发change事件
    event.target.value = ''
  }
}

// 初始化表单数据
const profileForm = ref({
  username: userStore.currentUser?.username || '',
  email: userStore.currentUser?.email || '',
  phone: userStore.currentUser?.phone || ''
})

// 监听用户信息变化
watch(() => userStore.currentUser, (newUser) => {
  if (newUser) {
    profileForm.value = {
      username: newUser.username || '',
      email: newUser.email || '',
      phone: newUser.phone || ''
    }
  }
}, { immediate: true })

// 更新个人资料
const updateProfile = async () => {
  updateProfileSuccess.value = false
  updateProfileError.value = ''
  isUpdatingProfile.value = true
  
  try {
    const result = await userStore.updateProfile({
      username: profileForm.value.username,
      email: profileForm.value.email,
      phone: profileForm.value.phone
    })
    
    if (result.success) {
      updateProfileSuccess.value = true
      // 3秒后隐藏成功消息
      setTimeout(() => {
        updateProfileSuccess.value = false
      }, 3000)
    } else {
      updateProfileError.value = result.error || '更新个人资料失败'
    }
  } catch (error) {
    console.error('Profile update error:', error)
    updateProfileError.value = '更新过程中发生错误'
  } finally {
    isUpdatingProfile.value = false
  }
}

// 收货地址相关
const addressForm = ref({
  name: '',
  phone: '',
  province: '',
  city: '',
  district: '',
  address: '',
  isDefault: false
})
const addressOperationMsg = ref({
  show: false,
  type: 'success',
  text: ''
})

// 编辑地址
const editAddress = (address) => {
  console.log('编辑地址:', address);
  editingAddressId.value = address.id
  
  // 解析地址字符串，提取省市区信息
  const addressParts = address.address ? address.address.split(' ') : [];
  let province = '', city = '', district = '', detailAddress = ''
  
  if (addressParts.length >= 3) {
    province = addressParts[0]
    city = addressParts[1]
    district = addressParts[2]
    detailAddress = addressParts.slice(3).join(' ')
  } else {
    // 如果地址格式不符合预期，则整个作为详细地址
    detailAddress = address.address || ''
  }
  
  addressForm.value = {
    name: address.recipientName || '',
    phone: address.recipientPhone || '',
    province: province,
    city: city,
    district: district,
    address: detailAddress,
    isDefault: address.isDefault || false
  }
  
  console.log('地址表单数据:', addressForm.value);
  showAddressForm.value = true
}

// 保存地址
const saveAddress = async () => {
  isProcessingAddress.value = true
  console.log('保存地址，ID:', editingAddressId.value);
  console.log('地址表单数据:', addressForm.value);
  
  try {
    let result
    
    if (editingAddressId.value) {
      // 更新现有地址
      result = await userStore.updateAddress(editingAddressId.value, addressForm.value)
      console.log('更新地址结果:', result);
    } else {
      // 添加新地址
      result = await userStore.addAddress(addressForm.value)
      console.log('添加地址结果:', result);
    }
    
    if (result.success) {
      showAddressForm.value = false
      showAddressMessage('success', editingAddressId.value ? '地址更新成功' : '地址添加成功')
    } else {
      // 即使API返回错误，也尝试刷新地址列表，因为数据库可能已经更新了
      try {
        await userStore.fetchUserAddresses();
        
        // 如果错误消息包含"已保存但返回数据时出错"，我们认为操作基本成功
        if (result.error && result.error.includes('已保存但返回数据时出错')) {
          showAddressForm.value = false
          showAddressMessage('success', '地址已保存，但返回数据时出错')
        } else {
          showAddressMessage('error', result.error || '操作失败')
        }
      } catch (fetchError) {
        console.error('Failed to refresh addresses:', fetchError);
        showAddressMessage('error', result.error || '操作失败')
      }
    }
  } catch (error) {
    console.error('Address operation error:', error)
    let errorMsg = '操作过程中发生错误';
    
    // 即使出现异常，也尝试刷新地址列表
    try {
      await userStore.fetchUserAddresses();
    } catch (fetchError) {
      console.error('Failed to refresh addresses after error:', fetchError);
    }
    
    if (error.response) {
      console.error('Error status:', error.response.status);
      console.error('Error data:', error.response.data);
      
      // 检查是否是已知的特殊情况
      if (error.response.data && error.response.data.message && 
          error.response.data.message.includes('地址已保存但返回数据时出错')) {
        showAddressForm.value = false
        showAddressMessage('success', '地址已保存，但返回数据时出错')
        isProcessingAddress.value = false
        return;
      }
      
      errorMsg = error.response.data?.error || errorMsg;
    }
    showAddressMessage('error', errorMsg)
  } finally {
    isProcessingAddress.value = false
  }
}

// 删除地址
const deleteAddress = async (addressId) => {
  if (!confirm('确定要删除此地址吗？')) return
  
  isProcessingAddress.value = true
  
  try {
    const result = await userStore.deleteAddress(addressId)
    
    if (result.success) {
      showAddressMessage('success', '地址删除成功')
    } else {
      showAddressMessage('error', result.error || '删除失败')
    }
  } catch (error) {
    console.error('Address delete error:', error)
    showAddressMessage('error', '删除过程中发生错误')
  } finally {
    isProcessingAddress.value = false
  }
}

// 设置默认地址
const setDefaultAddress = async (addressId) => {
  isProcessingAddress.value = true
  
  try {
    const result = await userStore.updateAddress(addressId, { isDefault: true })
    
    if (result.success) {
      showAddressMessage('success', '默认地址设置成功')
    } else {
      showAddressMessage('error', result.error || '设置失败')
    }
  } catch (error) {
    console.error('Set default address error:', error)
    showAddressMessage('error', '设置过程中发生错误')
  } finally {
    isProcessingAddress.value = false
  }
}

// 显示地址操作消息
const showAddressMessage = (type, text) => {
  addressOperationMsg.value = {
    show: true,
    type,
    text
  }
  
  setTimeout(() => {
    addressOperationMsg.value.show = false
  }, 3000)
}

// 密码修改相关
const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})
const showOldPassword = ref(false)
const showNewPassword = ref(false)
const showConfirmPassword = ref(false)
const isChangingPassword = ref(false)
const passwordUpdateMsg = ref({
  show: false,
  type: 'success',
  text: ''
})

// 计算密码强度
const passwordStrength = computed(() => {
  const password = passwordForm.value.newPassword
  
  if (!password) {
    return { level: '', text: '', percent: 0 }
  }
  
  // 密码强度评估
  let strength = 0
  
  // 长度检查
  if (password.length >= 8) strength += 1
  if (password.length >= 12) strength += 1
  
  // 复杂度检查
  if (/[A-Z]/.test(password)) strength += 1
  if (/[a-z]/.test(password)) strength += 1
  if (/[0-9]/.test(password)) strength += 1
  if (/[^A-Za-z0-9]/.test(password)) strength += 1
  
  // 结果映射
  const strengthMap = {
    0: { level: 'weak', text: '弱', percent: 20 },
    1: { level: 'weak', text: '弱', percent: 20 },
    2: { level: 'medium', text: '中', percent: 50 },
    3: { level: 'medium', text: '中', percent: 50 },
    4: { level: 'strong', text: '强', percent: 80 },
    5: { level: 'strong', text: '强', percent: 80 },
    6: { level: 'very-strong', text: '非常强', percent: 100 },
  }
  
  return strengthMap[strength]
})

// 密码有效性检查
const isPasswordValid = computed(() => {
  const { newPassword, confirmPassword } = passwordForm.value
  
  if (!newPassword || !confirmPassword) return false
  if (newPassword !== confirmPassword) return false
  if (newPassword.length < 6) return false
  
  return true
})

// 修改密码
const changePassword = async () => {
  if (!isPasswordValid.value) {
    showPasswordMessage('error', '请确保两次输入的新密码一致，且密码长度不少于6位')
    return
  }
  
  isChangingPassword.value = true
  
  try {
    const result = await userStore.changePassword(
      passwordForm.value.oldPassword,
      passwordForm.value.newPassword
    )
    
    if (result.success) {
      // 清空表单
      passwordForm.value = {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      }
      
      showPasswordMessage('success', result.message || '密码修改成功')
    } else {
      showPasswordMessage('error', result.error || '密码修改失败')
    }
  } catch (error) {
    console.error('Password change error:', error)
    showPasswordMessage('error', '修改过程中发生错误')
  } finally {
    isChangingPassword.value = false
  }
}

// 显示密码修改消息
const showPasswordMessage = (type, text) => {
  passwordUpdateMsg.value = {
    show: true,
    type,
    text
  }
  
  setTimeout(() => {
    passwordUpdateMsg.value.show = false
  }, 3000)
}

// 计算属性
const userName = computed(() => {
  return userStore.currentUser?.username || '未登录'
})

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '未知'
  return new Date(dateString).toLocaleDateString('zh-CN')
}

// 退出登录
const logout = () => {
  if (confirm('确定要退出登录吗？')) {
    userStore.logout()
    router.push('/login')
  }
}
</script>

<style scoped>
.profile-page {
  min-height: 100vh;
}

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

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
  padding-bottom: 15px;
  border-bottom: 1px solid #ebeef5;
}

.section-title {
  font-size: 1.6rem;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.profile-container {
  display: flex;
  gap: 30px;
  margin-top: 20px;
}

/* 左侧边栏 */
.profile-sidebar {
  width: 280px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  flex-shrink: 0;
}

.user-info {
  padding: 25px 20px;
  text-align: center;
  background: linear-gradient(to right, #4158d0, #c850c0);
  color: white;
}

.avatar-container {
  position: relative;
  width: 120px;
  height: 120px;
  margin: 0 auto 15px;
  border-radius: 50%;
  overflow: hidden;
  box-shadow: 0 3px 10px rgba(0, 0, 0, 0.2);
  background-color: #f0f0f0;
}

.user-avatar {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.avatar-upload {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(0, 0, 0, 0.6);
  color: white;
  padding: 5px 0;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  opacity: 0;
}

.avatar-container:hover .avatar-upload {
  opacity: 1;
}

.avatar-uploading {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 24px;
}

.avatar-error, .avatar-success {
  text-align: center;
  margin-bottom: 10px;
  font-size: 0.85rem;
  padding: 5px;
}

.user-name {
  font-size: 1.3rem;
  font-weight: 600;
  margin-top: 10px;
}

.nav-menu {
  padding: 15px 0;
}

.nav-item {
  display: flex;
  align-items: center;
  padding: 15px 20px;
  color: #606266;
  text-decoration: none;
  transition: all 0.3s;
  border-left: 4px solid transparent;
  font-size: 1rem;
}

.nav-item i {
  margin-right: 12px;
  width: 20px;
  text-align: center;
  font-size: 1.1rem;
}

.nav-item:hover {
  background: #f5f7fa;
  color: #409EFF;
}

.nav-item.active {
  background: #ecf5ff;
  color: #409EFF;
  border-left-color: #409EFF;
  font-weight: 500;
}

.nav-item.logout {
  margin-top: 20px;
  border-top: 1px solid #ebeef5;
  color: #f56c6c;
}

.nav-item.logout:hover {
  background: #fef0f0;
}

/* 右侧内容区 */
.profile-content {
  flex: 1;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  padding: 25px;
  min-height: 600px;
}

.tab-title {
  font-size: 1.4rem;
  color: #303133;
  margin-top: 0;
  margin-bottom: 25px;
  padding-bottom: 15px;
  border-bottom: 1px solid #ebeef5;
  font-weight: 600;
}

/* 表单样式 */
.form-row {
  display: flex;
  gap: 30px;
  margin-bottom: 20px;
}

.form-group {
  flex: 1;
  margin-bottom: 20px;
  position: relative;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  color: #606266;
  font-size: 0.95rem;
  font-weight: 500;
}

.form-group input[type="text"],
.form-group input[type="email"],
.form-group input[type="tel"],
.form-group input[type="password"] {
  width: 100%;
  padding: 12px 15px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 14px;
  transition: all 0.3s;
  color: #606266;
  background-color: #fff;
}

.form-group input:focus {
  outline: none;
  border-color: #409EFF;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

.form-group input:disabled {
  background-color: #f5f7fa;
  color: #909399;
  cursor: not-allowed;
  border-color: #e4e7ed;
}

.form-actions {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.btn-primary {
  background: #409EFF;
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 0.95rem;
  font-weight: 500;
  letter-spacing: 0.5px;
}

.btn-primary:hover {
  background: #66b1ff;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.btn-primary:disabled {
  background: #a0cfff;
  cursor: not-allowed;
  box-shadow: none;
}

.btn-cancel {
  background: #f5f7fa;
  color: #606266;
  border: 1px solid #dcdfe6;
  padding: 12px 24px;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 0.95rem;
  font-weight: 500;
}

.btn-cancel:hover {
  color: #409EFF;
  border-color: #c6e2ff;
  background-color: #ecf5ff;
}

/* 消息提示 */
.message {
  padding: 12px 15px;
  border-radius: 4px;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
}

.message i {
  margin-right: 10px;
  font-size: 1.1rem;
}

.success-message,
.message.success {
  background-color: #f0f9eb;
  color: #67c23a;
  border-left: 4px solid #67c23a;
}

.error-message,
.message.error {
  background-color: #fef0f0;
  color: #f56c6c;
  border-left: 4px solid #f56c6c;
}

/* 地址管理 */
.tab-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
  padding-bottom: 12px;
  border-bottom: 1px solid #ebeef5;
}

.btn-add {
  background: #409EFF;
  color: white;
  border: none;
  padding: 8px 15px;
  border-radius: 4px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 0.9rem;
}

.btn-add:hover {
  background: #66b1ff;
}

.empty-data {
  text-align: center;
  padding: 40px 0;
  color: #909399;
}

.empty-data i {
  font-size: 3rem;
  margin-bottom: 15px;
  opacity: 0.5;
}

.address-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.address-card {
  border: 1px solid #ebeef5;
  border-radius: 6px;
  padding: 18px;
  transition: all 0.3s;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.address-card:hover {
  border-color: #c6e2ff;
  background-color: #ecf5ff;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.address-header {
  margin-bottom: 10px;
}

.recipient {
  font-weight: 600;
  margin-right: 15px;
  font-size: 1.05rem;
  color: #303133;
}

.phone {
  color: #606266;
}

.default-tag {
  background: #409EFF;
  color: white;
  padding: 3px 8px;
  border-radius: 3px;
  font-size: 0.8rem;
  margin-left: 10px;
  font-weight: normal;
}

.address-detail {
  color: #606266;
  font-size: 0.95rem;
  line-height: 1.5;
}

.address-actions {
  display: flex;
  align-items: center;
  gap: 15px;
  flex-shrink: 0;
}

.address-actions a {
  color: #409EFF;
  text-decoration: none;
  font-size: 0.9rem;
  padding: 2px 0;
}

.address-actions a:hover {
  color: #66b1ff;
  text-decoration: underline;
}

.address-form-container {
  border: 1px solid #ebeef5;
  border-radius: 6px;
  padding: 25px;
  margin-top: 20px;
  background-color: #fafafa;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.address-form-container h4 {
  margin-top: 0;
  margin-bottom: 20px;
  color: #303133;
  font-size: 1.1rem;
  font-weight: 600;
  padding-bottom: 12px;
  border-bottom: 1px solid #ebeef5;
}

.checkbox-group {
  display: flex;
  align-items: center;
  margin-top: 5px;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  font-size: 0.95rem;
  color: #303133;
}

.checkbox-label input[type="checkbox"] {
  width: 16px;
  height: 16px;
  cursor: pointer;
}

/* 密码修改 */
.password-input {
  position: relative;
}

.toggle-password {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  color: #909399;
  cursor: pointer;
  transition: color 0.2s;
  padding: 5px;
}

.toggle-password:hover {
  color: #409EFF;
}

.password-form {
  max-width: 550px;
}

.password-strength {
  margin-top: 18px;
  margin-bottom: 24px;
  background-color: #f8f8f8;
  padding: 15px;
  border-radius: 6px;
  border: 1px solid #ebeef5;
}

.strength-label {
  margin-bottom: 8px;
  color: #606266;
  font-size: 0.95rem;
  font-weight: 500;
}

.strength-meter {
  height: 8px;
  background-color: #ebeef5;
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 8px;
}

.strength-value {
  height: 100%;
  transition: width 0.3s;
}

.strength-value.weak {
  background-color: #f56c6c;
}

.strength-value.medium {
  background-color: #e6a23c;
}

.strength-value.strong {
  background-color: #67c23a;
}

.strength-value.very-strong {
  background-color: #409EFF;
}

.strength-text {
  margin-top: 5px;
  font-size: 0.95rem;
  text-align: right;
  font-weight: 500;
}

.strength-text.weak {
  color: #f56c6c;
}

.strength-text.medium {
  color: #e6a23c;
}

.strength-text.strong {
  color: #67c23a;
}

.strength-text.very-strong {
  color: #409EFF;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .profile-container {
    flex-direction: column;
  }
  
  .profile-sidebar {
    width: 100%;
  }
  
  .form-row {
    flex-direction: column;
    gap: 0;
  }
  
  .form-group {
    margin-bottom: 15px;
  }
  
  .container {
    padding: 20px 15px;
  }
  
  .profile-content {
    padding: 20px 15px;
  }
  
  .profile-grid {
    grid-template-columns: 1fr;
    gap: 10px;
  }
}

@media (min-width: 769px) and (max-width: 1024px) {
  .form-row {
    gap: 20px;
  }
  
  .form-group input[type="text"],
  .form-group input[type="email"],
  .form-group input[type="tel"],
  .form-group input[type="password"] {
    padding: 10px 12px;
  }
  
  .profile-sidebar {
    width: 240px;
  }
  
  .profile-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 15px;
  }
}

/* 个人资料表单布局 */
.profile-form {
  max-width: 800px;
}

.profile-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 15px;
  margin-bottom: 20px;
}

/* 压缩的表单元素 */
.form-group.compact {
  margin-bottom: 15px;
}

.form-group.compact label {
  display: block;
  margin-bottom: 4px;
  color: #606266;
  font-size: 0.9rem;
  font-weight: 500;
}

.form-group.compact input[type="text"],
.form-group.compact input[type="email"],
.form-group.compact input[type="tel"],
.form-group.compact input[type="password"] {
  width: 100%;
  padding: 8px 10px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 13px;
  transition: all 0.3s;
  color: #606266;
  background-color: #fff;
  height: 36px;
}

.form-group.compact input:focus {
  outline: none;
  border-color: #409EFF;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

.form-group.compact input:disabled {
  background-color: #f5f7fa;
  color: #909399;
  cursor: not-allowed;
  border-color: #e4e7ed;
}
</style> 