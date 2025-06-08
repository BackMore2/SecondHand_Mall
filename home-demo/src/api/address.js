import api from './auth';

/**
 * 获取用户的所有收货地址
 * @param {number} userId 用户ID
 * @returns {Promise<Array>} 收货地址列表
 */
export const getUserAddresses = async (userId) => {
  try {
    const response = await api.get(`/addresses/user/${userId}`);
    return response.data;
  } catch (error) {
    console.error('获取收货地址失败:', error);
    throw error;
  }
};

/**
 * 获取用户的默认收货地址
 * @param {number} userId 用户ID
 * @returns {Promise<Object>} 默认收货地址
 */
export const getDefaultAddress = async (userId) => {
  try {
    const response = await api.get(`/addresses/user/${userId}/default`);
    return response.data;
  } catch (error) {
    console.error('获取默认地址失败:', error);
    throw error;
  }
};

/**
 * 添加收货地址
 * @param {number} userId 用户ID
 * @param {Object} address 地址信息
 * @returns {Promise<Object>} 新增的地址
 */
export const addAddress = async (userId, address) => {
  try {
    const response = await api.post(`/addresses/user/${userId}`, address);
    return response.data;
  } catch (error) {
    console.error('添加收货地址失败:', error);
    throw error;
  }
};

/**
 * 更新收货地址
 * @param {number} addressId 地址ID
 * @param {Object} address 地址信息
 * @returns {Promise<Object>} 更新后的地址
 */
export const updateAddress = async (addressId, address) => {
  try {
    const response = await api.put(`/addresses/${addressId}`, address);
    return response.data;
  } catch (error) {
    console.error('更新收货地址失败:', error);
    throw error;
  }
};

/**
 * 删除收货地址
 * @param {number} addressId 地址ID
 * @returns {Promise<Object>} 操作结果
 */
export const deleteAddress = async (addressId) => {
  try {
    const response = await api.delete(`/addresses/${addressId}`);
    return response.data;
  } catch (error) {
    console.error('删除收货地址失败:', error);
    throw error;
  }
};

/**
 * 设置默认收货地址
 * @param {number} addressId 地址ID
 * @returns {Promise<Object>} 操作结果
 */
export const setDefaultAddress = async (addressId) => {
  try {
    const response = await api.put(`/addresses/${addressId}/default`);
    return response.data;
  } catch (error) {
    console.error('设置默认地址失败:', error);
    throw error;
  }
}; 