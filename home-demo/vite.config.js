import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path' // 需要先安装 path 模块：npm install path

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'), // 将 @ 指向 src 目录
    }
  }
})
