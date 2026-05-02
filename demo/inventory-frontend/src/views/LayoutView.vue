<template>
  <el-container class="layout-container">
    <!-- ====== Sidebar ====== -->
    <el-aside class="layout-sidebar" width="220px">
      <!-- Brand -->
      <div class="sidebar-brand">
        <div class="brand-icon-wrapper">
          <el-icon :size="22"><Box /></el-icon>
        </div>
        <span class="brand-text">进销存系统</span>
      </div>

      <!-- Navigation -->
      <el-menu
        :default-active="route.path"
        class="sidebar-menu"
        background-color="transparent"
        text-color="#a6b1cc"
        active-text-color="#e94560"
        @select="handleMenuSelect"
      >
        <el-menu-item index="/dashboard">
          <el-icon><Odometer /></el-icon>
          <span>仪表盘</span>
        </el-menu-item>

        <el-menu-item index="/products">
          <el-icon><Box /></el-icon>
          <span>库存管理</span>
        </el-menu-item>

        <div v-if="auth.isAdmin" class="menu-divider">
          <span class="divider-label">管理员</span>
        </div>

        <el-menu-item v-if="auth.isAdmin" index="/admin/users">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>

        <el-menu-item v-if="auth.isAdmin" index="/admin/records">
          <el-icon><Document /></el-icon>
          <span>出入库记录</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <!-- ====== Main Area ====== -->
    <el-container class="layout-main-wrapper">
      <!-- Header -->
      <el-header class="layout-header" height="56px">
        <div class="header-left">
          <span class="header-title">{{ route.meta.title || '进销存系统' }}</span>
        </div>

        <div class="header-right">
          <div class="user-info">
            <div class="user-avatar">
              <el-icon :size="16"><UserFilled /></el-icon>
            </div>
            <span class="username">{{ auth.user?.username }}</span>
            <el-tag
              :type="auth.isAdmin ? 'danger' : 'info'"
              size="small"
              class="role-tag"
            >
              {{ auth.isAdmin ? '管理员' : '普通用户' }}
            </el-tag>
          </div>

          <el-button
            class="logout-btn"
            :icon="SwitchButton"
            @click="handleLogout"
            text
          >
            退出登录
          </el-button>
        </div>
      </el-header>

      <!-- Content -->
      <el-main class="layout-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { UserFilled } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()

function handleMenuSelect(path) {
  if (path !== route.path) {
    router.push(path)
  }
}

function handleLogout() {
  auth.logout()
  router.push('/login')
}
</script>

<style scoped>
/* ====== Layout Container ====== */
.layout-container {
  height: 100vh;
  overflow: hidden;
}

/* ====== Sidebar ====== */
.layout-sidebar {
  background-color: #1a1a2e;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-shadow: 2px 0 12px rgba(0, 0, 0, 0.15);
  position: relative;
  z-index: 10;
}

/* Brand */
.sidebar-brand {
  padding: 22px 20px 18px;
  display: flex;
  align-items: center;
  gap: 12px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
  flex-shrink: 0;
}

.brand-icon-wrapper {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: linear-gradient(135deg, #e94560, #c23152);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}

.brand-text {
  color: #fff;
  font-size: 18px;
  font-weight: 700;
  letter-spacing: 1px;
  white-space: nowrap;
}

/* Menu */
.sidebar-menu {
  border-right: none !important;
  flex: 1;
  padding-top: 8px;
}

.sidebar-menu :deep(.el-menu-item) {
  margin: 2px 8px;
  border-radius: 8px;
  height: 44px;
  line-height: 44px;
  font-size: 14px;
  transition: all 0.2s ease;
}

.sidebar-menu :deep(.el-menu-item:hover) {
  background-color: rgba(233, 69, 96, 0.08) !important;
  color: #e94560;
}

.sidebar-menu :deep(.el-menu-item.is-active) {
  background: linear-gradient(90deg, rgba(233, 69, 96, 0.18), rgba(233, 69, 96, 0.06)) !important;
  color: #e94560;
  font-weight: 600;
  border-right: 3px solid #e94560;
}

.sidebar-menu :deep(.el-menu-item .el-icon) {
  font-size: 18px;
}

/* Menu Divider */
.menu-divider {
  padding: 16px 28px 6px;
  flex-shrink: 0;
}

.divider-label {
  font-size: 11px;
  color: rgba(166, 177, 204, 0.45);
  text-transform: uppercase;
  letter-spacing: 2px;
  font-weight: 600;
}

/* ====== Main Wrapper ====== */
.layout-main-wrapper {
  flex-direction: column;
  overflow: hidden;
}

/* ====== Header ====== */
.layout-header {
  background: #fff;
  border-bottom: 1px solid #e8eaed;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  flex-shrink: 0;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
  z-index: 5;
}

.header-left {
  display: flex;
  align-items: center;
}

.header-title {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a2e;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

/* User Info */
.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-avatar {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background: #f0f2f5;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #909399;
}

.username {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.role-tag {
  font-weight: 500;
}

/* Logout Button */
.logout-btn {
  color: #e94560;
  font-size: 13px;
  padding: 6px 12px;
  border-radius: 6px;
  transition: all 0.2s ease;
}

.logout-btn:hover {
  background-color: rgba(233, 69, 96, 0.08);
}

/* ====== Main Content ====== */
.layout-main {
  padding: 20px;
  background: #f0f2f5;
  overflow-y: auto;
  flex: 1;
}
</style>
