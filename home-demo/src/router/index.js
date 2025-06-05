import { createRouter, createWebHistory } from 'vue-router';
import HomePage from '@/views/HomePage.vue';
import CartPage from '@/views/CartPage.vue';
import Login from '@/views/Login.vue';
import Register from '@/views/Register.vue';
import ForgotPassword from '@/views/ForgotPassword.vue';
import UserProfile from '@/views/UserProfile.vue';
import OrdersPage from '@/views/OrdersPage.vue';
import OrderDetailPage from '@/views/OrderDetailPage.vue';
import OrderCreatePage from '@/views/OrderCreatePage.vue';
import OrderPayPage from '@/views/OrderPayPage.vue';
import ProductDetailPage from '@/views/ProductDetailPage.vue';
import ProductPublishPage from '@/views/ProductPublishPage.vue';

// 修复变量名称拼写错误
const routes = [
    { path: '/', redirect: '/home' },
    { path: '/home' , component: HomePage },
    { path: '/cart', component: CartPage },
    { path: '/login', component: Login },
    { path: '/register', component: Register },
    { path: '/forgot-password', component: ForgotPassword },
    { path: '/user/profile', component: UserProfile },
    { path: '/orders', component: OrdersPage },
    { path: '/orders/:id', component: OrderDetailPage },
    { path: '/order/create', component: OrderCreatePage },
    { path: '/order/pay/:id', component: OrderPayPage },
    { path: '/product/publish', component: ProductPublishPage },
    { path: '/product/:id', component: ProductDetailPage }
];

const router = createRouter({
    history: createWebHistory(),
    routes
});

export default router;