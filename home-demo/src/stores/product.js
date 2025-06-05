import { defineStore } from 'pinia';
import { mockProducts } from '@/mocks/products';

export const useProductStore = defineStore('product', {
  state: () => ({
    products: JSON.parse(JSON.stringify(mockProducts)), // 深拷贝商品数据防止互相影响
    nextId: 100, // 设置一个较大的初始ID，避免和mock数据冲突
    categories: [
      { id: 1, name: '电子产品' },
      { id: 2, name: '服装鞋帽' },
      { id: 3, name: '图书音像' },
      { id: 4, name: '家具家电' },
      { id: 5, name: '运动户外' },
      { id: 6, name: '其他' }
    ]
  }),
  
  getters: {
    // 获取所有在售商品
    availableProducts: (state) => state.products.filter(p => p.status !== 'offline'),
    
    // 根据ID获取商品
    getProductById: (state) => (id) => state.products.find(p => p.id === Number(id)),
    
    // 获取用户发布的商品
    getUserProducts: (state) => (userId) => 
      state.products.filter(p => p.sellerId === userId),
      
    // 获取分类列表
    getCategoryList: (state) => state.categories,
    
    // 根据分类ID获取分类名称
    getCategoryNameById: (state) => (id) => {
      const category = state.categories.find(c => c.id === Number(id));
      return category ? category.name : '未分类';
    }
  },
  
  actions: {
    // 添加新商品
    addProduct(productData) {
      try {
        const newProduct = {
          id: this.nextId++,
          ...productData,
          publishTime: new Date().toISOString(),
          status: 'online', // 商品状态：online-上架，offline-下架
          views: 0,
          sales: 0
        };
        
        this.products.push(newProduct);
        
        return { success: true, product: newProduct };
      } catch (error) {
        console.error('Failed to add product:', error);
        return { success: false, error: '添加商品失败' };
      }
    },
    
    // 更新商品信息
    updateProduct(productId, productData) {
      try {
        const index = this.products.findIndex(p => p.id === Number(productId));
        
        if (index === -1) {
          return { success: false, error: '商品不存在' };
        }
        
        // 合并更新数据，保留不变的字段
        this.products[index] = {
          ...this.products[index],
          ...productData,
          lastUpdated: new Date().toISOString()
        };
        
        return { success: true, product: this.products[index] };
      } catch (error) {
        console.error('Failed to update product:', error);
        return { success: false, error: '更新商品失败' };
      }
    },
    
    // 商品上架
    publishProduct(productId) {
      try {
        const product = this.products.find(p => p.id === Number(productId));
        
        if (!product) {
          return { success: false, error: '商品不存在' };
        }
        
        product.status = 'online';
        product.lastUpdated = new Date().toISOString();
        
        return { success: true, product };
      } catch (error) {
        console.error('Failed to publish product:', error);
        return { success: false, error: '商品上架失败' };
      }
    },
    
    // 商品下架
    unpublishProduct(productId) {
      try {
        const product = this.products.find(p => p.id === Number(productId));
        
        if (!product) {
          return { success: false, error: '商品不存在' };
        }
        
        product.status = 'offline';
        product.lastUpdated = new Date().toISOString();
        
        return { success: true, product };
      } catch (error) {
        console.error('Failed to unpublish product:', error);
        return { success: false, error: '商品下架失败' };
      }
    },
    
    // 删除商品
    deleteProduct(productId) {
      try {
        const index = this.products.findIndex(p => p.id === Number(productId));
        
        if (index === -1) {
          return { success: false, error: '商品不存在' };
        }
        
        this.products.splice(index, 1);
        
        return { success: true };
      } catch (error) {
        console.error('Failed to delete product:', error);
        return { success: false, error: '删除商品失败' };
      }
    },
    
    // 增加商品浏览量
    incrementProductViews(productId) {
      const product = this.products.find(p => p.id === Number(productId));
      if (product) {
        product.views = (product.views || 0) + 1;
      }
    }
  },
  
  // 数据持久化
  persist: {
    enabled: true,
    strategies: [
      {
        key: 'products',
        storage: localStorage
      }
    ]
  }
}); 