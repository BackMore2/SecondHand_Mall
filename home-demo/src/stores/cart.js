import { defineStore } from 'pinia';
import * as cartApi from '@/api/cart';
import * as productApi from '@/api/product';
import { useUserStore } from './user';

export const useCartStore = defineStore('cart', {
    state: () => ({
        items: [], // 购物车商品
        loading: false, // 加载状态
        productLoading: false, // 加载商品状态
        error: null, // 错误信息
        isSync: false, // 是否已与后端同步
        userId: null, // 当前用户ID
        productCache: {}, // 缓存商品数据
        operationInProgress: false // 操作进行中标志，防止重复操作
    }),
    actions: {
        // 初始化购物车数据
async initCart() {
    const userStore = useUserStore();
    this.userId = userStore.currentUser?.id;
    
    try {
        this.loading = true;
        this.error = null;
        
        if (!this.userId) {
            console.log('用户未登录，使用本地购物车');
            this.isSync = false;
            return;
        }
        
        // 检查token
        const token = localStorage.getItem('token');
        if (!token) {
            console.log('未获取到认证令牌，使用本地购物车');
            this.isSync = false;
            return;
        }
        
        try {
            // 从API获取购物车数据
            const cartData = await cartApi.getCart(this.userId);
            
            // 使用API提供的方法格式化数据
            const formattedItems = cartApi.formatCartData(cartData);
            
            // 更新购物车项
            this.items = formattedItems;
            this.isSync = true;
            
            // 如果购物车有商品，确保我们有最新的商品数据（价格、库存等）
            if (this.items.length > 0) {
                await this.updateProductDetails();
            }
            
            console.log('购物车初始化完成，同步状态:', this.isSync);
        } catch (error) {
            console.error('初始化购物车失败:', error);
            
            if (error.response && error.response.status === 403) {
                this.error = '权限不足，无法访问购物车';
                // 可能需要重新登录
                console.warn('权限验证失败，Token可能已失效');
            } else if (error.message === '用户未登录') {
                this.error = '请先登录';
                this.isSync = false;
            } else {
                this.error = '获取购物车数据失败';
            }
            
            this.isSync = false;
        }
    } catch (error) {
        console.error('购物车初始化出错:', error);
        this.error = '购物车初始化失败';
        this.isSync = false;
    } finally {
        this.loading = false;
    }
        },
        
        // 更新购物车中商品的详细信息（库存、价格等）
        async updateProductDetails() {
            try {
                this.productLoading = true;
                
                // 对购物车中的每个商品ID，获取最新的商品数据
                for (const item of this.items) {
                    // 如果缓存中没有此商品，或者缓存时间超过5分钟，则重新获取
                    const cachedProduct = this.productCache[item.id];
                    const now = Date.now();
                    const needsRefresh = !cachedProduct || (now - cachedProduct.timestamp > 5 * 60 * 1000);
                    
                    if (needsRefresh) {
                        try {
                            const productData = await productApi.getProductById(item.id);
                            if (productData) {
                                // 更新缓存
                                this.productCache[item.id] = {
                                    data: productData,
                                    timestamp: now
                                };
                                
                                // 更新购物车项的商品信息
                                item.price = productData.price;
                                item.stock = productData.stock;
                                item.name = productData.name;
                                item.image = productData.mainImage || productData.main_image || productData.image;
                                
                                // 如果数量超过库存，调整数量
                                if (item.quantity > productData.stock) {
                                    item.quantity = Math.max(1, productData.stock);
                                    
                                    // 如果已登录且有cartItemId，同步到服务器
                                    if (this.userId && item.cartItemId) {
                                        await this.syncItemQuantityToServer(item.cartItemId, item.quantity);
                                    }
                                }
                            }
                        } catch (err) {
                            console.error(`获取商品 ${item.id} 详情失败:`, err);
                        }
                    } else {
                        // 使用缓存数据
                        const productData = cachedProduct.data;
                        item.price = productData.price;
                        item.stock = productData.stock;
                        item.name = productData.name;
                        item.image = productData.mainImage || productData.main_image || productData.image;
                    }
                }
            } catch (error) {
                console.error('更新商品详情失败:', error);
            } finally {
                this.productLoading = false;
            }
        },
        
        // 同步购物车项数量到服务器
        async syncItemQuantityToServer(cartItemId, quantity) {
            try {
                await cartApi.updateCartItemQuantity(cartItemId, quantity);
                this.isSync = true;
                return true;
            } catch (error) {
                console.error('同步数量到服务器失败:', error);
                this.isSync = false;
                this.error = '更新数量失败';
                return false;
            }
        },
        
        // 添加商品到购物车
async addToCart(productId, quantity = 1) {
    if (this.operationInProgress) {
        return Promise.reject('操作正在进行中，请稍候');
    }
    
    try {
        this.operationInProgress = true;
        
        // 检查用户登录状态
        if (this.userId && !localStorage.getItem('token')) {
            console.error('未获取到认证令牌，无法同步到服务器');
            return Promise.reject('请先登录');
        }
        
        // 先获取最新的商品信息
        const productData = await productApi.getProductById(productId);
        
        if (!productData || productData.stock <= 0) {
            console.error('商品库存不足');
            return Promise.reject('商品库存不足');
        }
        
        // 首先更新本地购物车
        const exist = this.items.find(item => item.id === productId);
        
        if (exist) {
            // 检查是否超过库存
            if (exist.quantity + quantity > productData.stock) {
                console.error('商品库存不足');
                return Promise.reject('商品库存不足');
            }
            
            // 更新本地数量
            const oldQuantity = exist.quantity;
            exist.quantity += quantity;
            
            // 如果已登录且有cartItemId，同步到服务器
            if (this.userId && exist.cartItemId) {
                const success = await this.syncItemQuantityToServer(exist.cartItemId, exist.quantity);
                
                if (!success) {
                    // 回滚本地更改
                    exist.quantity = oldQuantity;
                    return Promise.reject('更新数量失败');
                }
            } else if (this.userId) {
                // 如果已登录但没有cartItemId，添加到服务器
                try {
                    const response = await cartApi.addToCart(this.userId, productId, quantity);
                    this.isSync = true;
                    
                    // 如果服务器返回了cartItemId，更新本地数据
                    if (response && response.id) {
                        exist.cartItemId = response.id;
                    }
                } catch (error) {
                    // 回滚本地更改
                    exist.quantity = oldQuantity;
                    
                    console.error('添加商品到服务器购物车失败:', error);
                    
                    // 处理特定错误
                    if (error.response && error.response.status === 403) {
                        this.error = '权限不足，无法添加购物车';
                        console.warn('权限验证失败，Token可能已失效');
                    } else if (error.message === '用户未登录') {
                        this.error = '请先登录';
                    } else {
                        this.error = '添加商品到购物车失败';
                    }
                    
                    this.isSync = false;
                    return Promise.reject(this.error);
                }
            }
        } else {
            // 创建新购物车项
            const newItem = { 
                id: productId,
                quantity: quantity, 
                selected: true,
                price: productData.price,
                name: productData.name,
                stock: productData.stock,
                image: productData.mainImage || productData.main_image || productData.image,
                cartItemId: null // 将在服务器同步后更新
            };
            
            // 如果已登录，同步到服务器
            if (this.userId) {
                try {
                    const response = await cartApi.addToCart(this.userId, productId, quantity);
                    this.isSync = true;
                    
                    // 如果服务器返回了cartItemId，更新本地数据
                    if (response && response.id) {
                        newItem.cartItemId = response.id;
                    }
                } catch (error) {
                    console.error('添加商品到服务器购物车失败:', error);
                    
                    // 处理特定错误
                    if (error.response && error.response.status === 403) {
                        this.error = '权限不足，无法添加购物车';
                        console.warn('权限验证失败，Token可能已失效');
                    } else if (error.message === '用户未登录') {
                        this.error = '请先登录';
                    } else {
                        this.error = '添加商品到购物车失败';
                    }
                    
                    this.isSync = false;
                    return Promise.reject(this.error);
                }
            }
            
            // 添加到本地购物车
            this.items.push(newItem);
        }
        
        return Promise.resolve('添加成功');
    } catch (error) {
        console.error('添加商品到购物车失败:', error);
        
        // 转换错误消息为用户友好的提示
        let errorMessage = '添加失败，请稍后重试';
        if (error.message === '用户未登录' || error === '请先登录') {
            errorMessage = '请先登录';
        } else if (error === '商品库存不足') {
            errorMessage = '商品库存不足';
        } else if (error.response && error.response.status === 403) {
            errorMessage = '权限不足，请重新登录';
        }
        
        return Promise.reject(errorMessage);
    } finally {
        this.operationInProgress = false;
    }
},
        
        // 从购物车移除商品
        async removeFromCart(itemId) {
            if (this.operationInProgress) {
                return Promise.reject('操作正在进行中，请稍候');
            }
            
            try {
                this.operationInProgress = true;
                const index = this.items.findIndex(item => item.id === itemId);
                
                if (index !== -1) {
                    const item = this.items[index];
                    const cartItemId = item.cartItemId;
                    
                    // 首先移除本地项目（保存备份以便出错时恢复）
                    const removedItem = { ...this.items[index] };
                    this.items.splice(index, 1);
                    
                    // 如果已登录且有cartItemId，同步到服务器
                    if (this.userId && cartItemId) {
                        try {
                            await cartApi.removeFromCart(cartItemId);
                            this.isSync = true;
                        } catch (error) {
                            console.error('从服务器购物车移除商品失败:', error);
                            this.error = '从购物车移除商品失败';
                            this.isSync = false;
                            
                            // 恢复本地项目
                            this.items.splice(index, 0, removedItem);
                            return Promise.reject(error);
                        }
                    }
                    
                    return Promise.resolve('移除成功');
                }
                
                return Promise.reject('商品不在购物车中');
            } catch (error) {
                console.error('从购物车移除商品失败:', error);
                return Promise.reject(error);
            } finally {
                this.operationInProgress = false;
            }
        },
        
        // 更新商品数量
        async updateQuantity(itemId, quantity) {
            if (this.operationInProgress) {
                return Promise.reject('操作正在进行中，请稍候');
            }
            
            if (quantity <= 0) {
                return this.removeFromCart(itemId);
            }
            
            try {
                this.operationInProgress = true;
                const item = this.items.find(item => item.id === itemId);
                
                if (item) {
                    // 首先获取最新的商品库存
                    let currentStock = item.stock;
                    
                    try {
                        const productData = await productApi.getProductById(itemId);
                        if (productData) {
                            currentStock = productData.stock;
                            // 更新缓存
                            this.productCache[itemId] = {
                                data: productData,
                                timestamp: Date.now()
                            };
                            
                            // 同步更新本地item的库存
                            item.stock = currentStock;
                        }
                    } catch (err) {
                        console.error(`获取商品 ${itemId} 详情失败:`, err);
                    }
                    
                    // 确保不超过库存
                    if (quantity > currentStock) {
                        return Promise.reject('商品库存不足');
                    }
                    
                    // 如果数量没有变化，不做任何操作
                    if (quantity === item.quantity) {
                        return Promise.resolve('数量未变更');
                    }
                    
                    // 更新购物车项数量
                    const oldQuantity = item.quantity;
                    item.quantity = quantity;
                    
                    // 如果已登录且有cartItemId，同步到服务器
                    if (this.userId && item.cartItemId) {
                        try {
                            await cartApi.updateCartItemQuantity(item.cartItemId, quantity);
                            this.isSync = true;
                        } catch (error) {
                            console.error('更新服务器购物车商品数量失败:', error);
                            this.error = '更新商品数量失败';
                            this.isSync = false;
                            
                            // 回滚本地更改
                            item.quantity = oldQuantity;
                            return Promise.reject(error);
                        }
                    }
                    
                    return Promise.resolve('更新成功');
                }
                
                return Promise.reject('商品不在购物车中');
            } catch (error) {
                console.error('更新购物车商品数量失败:', error);
                return Promise.reject(error);
            } finally {
                this.operationInProgress = false;
            }
        },
        
        // 选择/取消选择单个商品
        toggleSelect(itemId) {
            const item = this.items.find(item => item.id === itemId);
            if (item) {
                item.selected = !item.selected;
            }
        },
        
        // 全选/取消全选
        toggleSelectAll(isSelected) {
            this.items.forEach(item => {
                // 只有库存大于0的商品才能被选中
                if (item.stock > 0) {
                    item.selected = isSelected;
                }
            });
        },
        
        // 清空购物车 - 使用逐个删除的方式
        async clearCart() {
            if (this.operationInProgress) {
                return Promise.reject('操作正在进行中，请稍候');
            }
            
            try {
                this.operationInProgress = true;
                console.log('开始清空购物车，用户ID:', this.userId);
                console.log('当前购物车项数量:', this.items.length);
                
                // 保存备份以便出错时恢复
                const backupItems = [...this.items];
                
                // 如果购物车为空，不需要进行任何操作
                if (backupItems.length === 0) {
                    console.log('购物车已经是空的，无需清空');
                    return Promise.resolve('购物车已经是空的');
                }
                
                // 如果已登录，先删除服务器数据
                if (this.userId) {
                    try {
                        console.log('开始逐个删除购物车项，总数:', backupItems.length);
                        
                        // 逐个删除购物车项
                        let successCount = 0;
                        let failCount = 0;
                        
                        // 顺序删除而不是并行，避免并发问题
                        for (const item of backupItems) {
                            if (item.cartItemId) {
                                try {
                                    console.log('删除购物车项:', item.cartItemId, '商品:', item.name);
                                    await cartApi.removeFromCart(item.cartItemId);
                                    successCount++;
                                } catch (e) {
                                    console.error(`删除购物车项 ${item.cartItemId} 失败:`, e);
                                    failCount++;
                                }
                            }
                        }
                        
                        console.log(`购物车项删除完成，成功: ${successCount}, 失败: ${failCount}`);
                        
                        // 只有在所有项都成功删除的情况下，才将同步状态设为true
                        if (failCount === 0) {
                        this.isSync = true;
                        } else {
                            this.isSync = false;
                            this.error = `部分购物车项删除失败(${failCount}/${backupItems.length})`;
                            console.warn(this.error);
                        }
                    } catch (error) {
                        console.error('清空服务器购物车失败:', error);
                        this.error = '清空购物车失败';
                        this.isSync = false;
                        
                        // 发生异常时保留本地数据
                        return Promise.reject(error);
                    }
                } else {
                    console.log('用户未登录，仅清空本地购物车');
                }
                
                // 清空本地购物车（无论是否登录都清空本地）
                this.items = [];
                
                console.log('购物车清空完成');
                return Promise.resolve('清空成功');
            } catch (error) {
                console.error('清空购物车失败:', error);
                return Promise.reject(error);
            } finally {
                this.operationInProgress = false;
            }
        },
        
        // 查找已售罄或库存不足的商品并更新
        checkOutOfStockItems() {
            const outOfStockItems = this.items.filter(item => item.stock <= 0);
            const lowStockItems = this.items.filter(item => item.stock < item.quantity);
            
            // 将已售罄的商品自动取消选择
            outOfStockItems.forEach(item => {
                item.selected = false;
            });
            
            // 调整数量超过库存的商品
            lowStockItems.forEach(async item => {
                const oldQuantity = item.quantity;
                item.quantity = item.stock;
                
                // 如果已登录且有cartItemId，同步到服务器
                if (this.userId && item.cartItemId) {
                    await this.syncItemQuantityToServer(item.cartItemId, item.quantity);
                }
            });
            
            return {
                outOfStockItems,
                lowStockItems
            };
        }
    },
    getters: {
        isAllSelected: state => state.items.length > 0 && state.items.every(i => i.selected),
        selectedItems: state => state.items.filter(i => i.selected),
        totalPrice: state => state.items.filter(i => i.selected).reduce((sum, i) => sum + i.price * i.quantity, 0),
        totalCount: state => state.items.reduce((sum, i) => sum + i.quantity, 0),
        inStockItems: state => state.items.filter(i => i.stock > 0),
        outOfStockItems: state => state.items.filter(i => i.stock <= 0)
    },
    // 持久化到 localStorage
    persist: {
        enabled: true,
        strategies: [
            {
                key: 'cart', // 存储键名
                storage: localStorage
            }
        ]
    }
});