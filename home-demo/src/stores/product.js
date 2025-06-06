import { defineStore } from 'pinia';
import { mockProducts } from '@/mocks/products';
import * as productApi from '@/api/product';

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
    ],
    loading: false,
    error: null
  }),
  
  getters: {
    // 获取所有在售商品
    availableProducts: (state) => state.products.filter(p => p.status === 1 || p.status === 'online'),
    
    // 根据ID获取商品
    getProductById: (state) => (id) => state.products.find(p => p.id === Number(id)),
    
    // 获取用户发布的商品
    getUserProducts: (state) => (userId) => 
      state.products.filter(p => p.seller_id === userId || p.sellerId === userId),
      
    // 获取分类列表
    getCategoryList: (state) => state.categories,
    
    // 根据分类ID获取分类名称
    getCategoryNameById: (state) => (id) => {
      const category = state.categories.find(c => c.id === Number(id));
      return category ? category.name : '未分类';
    }
  },
  
  actions: {
    // 初始化加载商品
    async fetchProducts() {
      this.loading = true;
      this.error = null;
      
      try {
        const products = await productApi.getAllProducts();
        // 处理返回的产品数据
        this.products = products.map(p => this.normalizeProduct(p));
        return { success: true };
      } catch (error) {
        console.error('Failed to fetch products:', error);
        this.error = '加载商品失败';
        return { success: false, error: this.error };
      } finally {
        this.loading = false;
      }
    },
    
    // 规范化产品数据（从数据库格式转为前端格式）
    normalizeProduct(product) {
      try {
        const normalizedProduct = { ...product };
        
        // 处理图片（将JSON字符串转换为数组）
        if (typeof normalizedProduct.images === 'string') {
          try {
            normalizedProduct.images = JSON.parse(normalizedProduct.images);
          } catch {
            normalizedProduct.images = [];
          }
        } else if (!Array.isArray(normalizedProduct.images)) {
          normalizedProduct.images = [];
        }
        
        // 处理交易方式
        normalizedProduct.supportMethods = {
          faceToFace: !!normalizedProduct.face_to_face,
          delivery: !!normalizedProduct.delivery
        };
        
        // 将数据库字段名映射为前端使用的驼峰命名字段
        if (normalizedProduct.seller_id && !normalizedProduct.sellerId) {
          normalizedProduct.sellerId = normalizedProduct.seller_id;
        }
        
        // 处理状态（适配前端格式）
        if (typeof normalizedProduct.status === 'number') {
          normalizedProduct.status = normalizedProduct.status === 1 ? 'online' : 'offline';
        }
        
        return normalizedProduct;
      } catch (error) {
        console.error('Error normalizing product:', error);
        return product;
      }
    },
    
    // 加载用户商品
    async fetchUserProducts() {
      this.loading = true;
      this.error = null;
      
      try {
        const userProducts = await productApi.getUserProducts();
        // 处理返回的产品数据
        const normalizedProducts = userProducts.map(p => this.normalizeProduct(p));
        
        // 将用户商品与已有商品合并，避免覆盖其他商品
        const existingIds = new Set(this.products.map(p => p.id));
        
        normalizedProducts.forEach(product => {
          if (existingIds.has(product.id)) {
            // 更新已存在的商品
            const index = this.products.findIndex(p => p.id === product.id);
            this.products[index] = product;
          } else {
            // 添加新商品
            this.products.push(product);
          }
        });
        
        return { success: true };
      } catch (error) {
        console.error('Failed to fetch user products:', error);
        this.error = '加载用户商品失败';
        return { success: false, error: this.error };
      } finally {
        this.loading = false;
      }
    },
    
    // 添加新商品
    async addProduct(productData) {
      this.loading = true;
      this.error = null;
      
      try {
        // 直接使用图片数组（包含Base64格式的图片数据）
        // 不需要额外的处理，后端会直接存储Base64数据
        
        // 调用API添加商品
        const newProduct = await productApi.addProduct(productData);
        
        // 将新商品添加到状态中（规范化数据）
        const normalizedProduct = this.normalizeProduct(newProduct);
        this.products.push(normalizedProduct);
        
        return { success: true, product: normalizedProduct };
      } catch (error) {
        console.error('Failed to add product:', error);
        this.error = '添加商品失败';
        return { success: false, error: this.error };
      } finally {
        this.loading = false;
      }
    },
    
    // 更新商品信息
    async updateProduct(productId, productData) {
      this.loading = true;
      this.error = null;
      
      try {
        // 直接使用图片数组（包含Base64格式的图片数据）
        // 不需要额外的处理，后端会直接存储Base64数据
        
        // 调用API更新商品
        const updatedProduct = await productApi.updateProduct(productId, productData);
        
        // 更新本地状态（规范化数据）
        const normalizedProduct = this.normalizeProduct(updatedProduct);
        const index = this.products.findIndex(p => p.id === Number(productId));
        if (index !== -1) {
          this.products[index] = normalizedProduct;
        }
        
        return { success: true, product: normalizedProduct };
      } catch (error) {
        console.error('Failed to update product:', error);
        this.error = '更新商品失败';
        return { success: false, error: this.error };
      } finally {
        this.loading = false;
      }
    },
    
    // 上传Base64图片
    async uploadBase64Image(base64Image) {
      try {
        // 将Base64转换为Blob对象
        const blob = await fetch(base64Image).then(res => res.blob());
        const file = new File([blob], "product_image.jpg", { type: "image/jpeg" });
        
        // 上传图片
        const response = await productApi.uploadProductImage(file);
        return { success: true, imageUrl: response.url };
      } catch (error) {
        console.error('Failed to upload image:', error);
        return { success: false, error: '上传图片失败' };
      }
    },
    
    // 商品上架
    async publishProduct(productId) {
      try {
        const result = await productApi.updateProductStatus(productId, true);
        
        // 更新本地状态
        const product = this.products.find(p => p.id === Number(productId));
        if (product) {
          product.status = 'online';
          // 同时更新数值状态，以便兼容
          product.status_val = 1;
        }
        
        return { success: true, product };
      } catch (error) {
        console.error('Failed to publish product:', error);
        return { success: false, error: '商品上架失败' };
      }
    },
    
    // 商品下架
    async unpublishProduct(productId) {
      try {
        const result = await productApi.updateProductStatus(productId, false);
        
        // 更新本地状态
        const product = this.products.find(p => p.id === Number(productId));
        if (product) {
          product.status = 'offline';
          // 同时更新数值状态，以便兼容
          product.status_val = 0;
        }
        
        return { success: true, product };
      } catch (error) {
        console.error('Failed to unpublish product:', error);
        return { success: false, error: '商品下架失败' };
      }
    },
    
    // 删除商品
    async deleteProduct(productId) {
      try {
        await productApi.deleteProduct(productId);
        
        // 从本地状态中删除
        const index = this.products.findIndex(p => p.id === Number(productId));
        if (index !== -1) {
          this.products.splice(index, 1);
        }
        
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