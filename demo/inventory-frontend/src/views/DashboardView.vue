<template>
  <div class="dashboard-page" v-loading="loading">
    <div class="dashboard-header">
      <div class="header-content">
        <h2 class="page-title">仪表盘</h2>
        <p class="welcome-msg">欢迎回来，{{ auth.user?.username }}</p>
      </div>
    </div>

    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <div class="stat-card stat-card--blue">
          <div class="stat-left">
            <el-icon :size="30" color="#409EFF"><Box /></el-icon>
          </div>
          <div class="stat-right">
            <span class="stat-label">商品总数</span>
            <span class="stat-value">{{ fmtInteger(stats.totalProducts) }}</span>
          </div>
        </div>
      </el-col>

      <el-col :span="6">
        <div class="stat-card stat-card--red">
          <div class="stat-left">
            <el-icon :size="30" color="#F56C6C"><WarningFilled /></el-icon>
          </div>
          <div class="stat-right">
            <span class="stat-label">库存预警</span>
            <span class="stat-value">{{ fmtInteger(stats.lowStockCount) }}</span>
            <span class="stat-sub">需要关注</span>
          </div>
        </div>
      </el-col>

      <el-col :span="6">
        <div class="stat-card stat-card--green">
          <div class="stat-left">
            <el-icon :size="30" color="#67C23A"><Upload /></el-icon>
          </div>
          <div class="stat-right">
            <span class="stat-label">本月入库</span>
            <span class="stat-value">{{ fmtInteger(stats.totalStockInThisMonth) }}</span>
          </div>
        </div>
      </el-col>

      <el-col :span="6">
        <div class="stat-card stat-card--orange">
          <div class="stat-left">
            <el-icon :size="30" color="#E6A23C"><Download /></el-icon>
          </div>
          <div class="stat-right">
            <span class="stat-label">本月出库</span>
            <span class="stat-value">{{ fmtInteger(stats.totalStockOutThisMonth) }}</span>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="stats-row">
      <el-col :span="24">
        <div class="stat-card stat-card--purple stat-card--full">
          <div class="stat-left">
            <el-icon :size="36" color="#a855f7"><Coin /></el-icon>
          </div>
          <div class="stat-right">
            <span class="stat-label">库存总价值</span>
            <span class="stat-value stat-value--lg">{{ fmtCurrency(stats.totalStockValue) }}</span>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { getDashboard } from '@/api'

const auth = useAuthStore()
const loading = ref(true)

const stats = reactive({
  totalProducts: 0,
  lowStockCount: 0,
  totalStockInThisMonth: 0,
  totalStockOutThisMonth: 0,
  totalStockValue: 0
})

function fmtInteger(val) {
  const n = Number(val)
  return Number.isNaN(n) ? '—' : n.toLocaleString()
}

function fmtCurrency(val) {
  const n = Number(val)
  return Number.isNaN(n) ? '—' : '¥' + n.toFixed(2)
}

onMounted(async () => {
  try {
    const res = await getDashboard()
    if (res.data) {
      stats.totalProducts = res.data.totalProducts ?? 0
      stats.lowStockCount = res.data.lowStockCount ?? 0
      stats.totalStockInThisMonth = res.data.totalStockInThisMonth ?? 0
      stats.totalStockOutThisMonth = res.data.totalStockOutThisMonth ?? 0
      stats.totalStockValue = res.data.totalStockValue ?? 0
    }
  } catch (e) {
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.dashboard-page {
  padding: 0;
}

.dashboard-header {
  margin-bottom: 24px;
}

.header-content {
  display: flex;
  align-items: baseline;
  gap: 16px;
}

.page-title {
  font-size: 22px;
  font-weight: 700;
  color: #1a1a2e;
  margin: 0;
}

.welcome-msg {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

.stats-row {
  margin-bottom: 20px;
}

.stats-row:last-child {
  margin-bottom: 0;
}

.stat-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  border-left: 4px solid transparent;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: box-shadow 0.25s ease, transform 0.25s ease;
  cursor: default;
  height: 100%;
  box-sizing: border-box;
}

.stat-card:hover {
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.stat-card--blue  { border-left-color: #409EFF; }
.stat-card--red   { border-left-color: #F56C6C; }
.stat-card--green { border-left-color: #67C23A; }
.stat-card--orange{ border-left-color: #E6A23C; }
.stat-card--purple{ border-left-color: #a855f7; }

.stat-card--full {
  padding: 24px 28px;
}

.stat-left {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-right {
  display: flex;
  flex-direction: column;
  gap: 4px;
  flex: 1;
  min-width: 0;
}

.stat-label {
  font-size: 12px;
  color: #909399;
  font-weight: 500;
  letter-spacing: 0.5px;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
  line-height: 1.2;
}

.stat-value--lg {
  font-size: 34px;
}

.stat-sub {
  font-size: 11px;
  color: #c0c4cc;
  margin-top: 2px;
}

.dashboard-page :deep(.el-loading-mask) {
  background-color: rgba(240, 242, 245, 0.6);
}

@media (max-width: 1200px) {
  .stat-value {
    font-size: 22px;
  }
  .stat-value--lg {
    font-size: 26px;
  }
}
</style>
