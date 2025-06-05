import { createApp } from 'vue'
import { createPinia } from 'pinia' // 导入 createPinia
import App from './App.vue'
import router from './router'

// 创建 Pinia 实例
const pinia = createPinia()

const app = createApp(App)

// 先注册 Pinia，再注册其他插件
app.use(pinia)
app.use(router)

app.mount('#app')