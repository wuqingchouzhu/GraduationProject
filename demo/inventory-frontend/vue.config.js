const { defineConfig } = require('@vue/cli-service')
module.exports = defineConfig({
  transpileDependencies: true,
  pages: {
    index: {
      entry: 'src/main.js',
      title: '仓库进销存管理系统',
    },
  },
  devServer: {
    port: 8081 // 强制前端使用 8081 端口
  }
})