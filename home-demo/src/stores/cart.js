import { defineStore } from 'pinia';
import { mockProducts } from '@/mocks/products';

export const useCartStore = defineStore('cart', {
    state: () => ({
        items: [], // 购物车商品
        products: mockProducts // 模拟商品数据
    }),
    actions: {
        // 添加商品到购物车
        addToCart(productId) {
            const product = this.products.find(p => p.id === productId);
            if (product && product.stock > 0) {
                const exist = this.items.find(item => item.id === productId);
                if (exist) {
                    exist.quantity += 1;
                } else {
                    this.items.push({ ...product, quantity: 1, selected: true });
                }
                product.stock--; // 模拟库存减少
            }
        },
        // 从购物车移除商品
        removeFromCart(itemId) {
            const index = this.items.findIndex(item => item.id === itemId);
            if (index !== -1) {
                const item = this.items.splice(index, 1)[0];
                const product = this.products.find(p => p.id === item.id);
                if (product) product.stock++; // 恢复库存
            }
        },
        // 更新商品数量
        updateQuantity(itemId, quantity) {
            const item = this.items.find(item => item.id === itemId);
            if (item) {
                const product = this.products.find(p => p.id === item.id);
                // 计算数量变化
                const diff = quantity - item.quantity;
                
                // 检查库存是否充足
                if (diff > 0 && product && product.stock >= diff) {
                    item.quantity = quantity;
                    product.stock -= diff;
                } else if (diff < 0) {
                    item.quantity = quantity;
                    if (product) product.stock += Math.abs(diff);
                } else if (diff > 0) {
                    alert('商品库存不足');
                }
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
                item.selected = isSelected;
            });
        }
    },
    getters: {
        isAllSelected: state => state.items.length > 0 && state.items.every(i => i.selected),
        selectedItems: state => state.items.filter(i => i.selected),
        totalPrice: state => state.items.filter(i => i.selected).reduce((sum, i) => sum + i.price * i.quantity, 0)
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