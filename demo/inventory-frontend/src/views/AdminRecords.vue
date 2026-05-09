<template>
  <div class="admin-records">
    <div class="filter-bar">
      <el-select
        v-model="filters.type"
        placeholder="记录类型"
        clearable
      >
        <el-option label="全部" value="" />
        <el-option label="入库" value="STOCK_IN" />
        <el-option label="出库" value="STOCK_OUT" />
      </el-select>

      <el-input-number
        v-model="filters.productId"
        placeholder="商品ID"
        :min="1"
        clearable
        controls-position="right"
      />

      <el-button type="primary" :icon="Search" @click="handleSearch">
        查询
      </el-button>
      <el-button :icon="Refresh" @click="handleReset">
        重置
      </el-button>
    </div>

    <el-table
      :data="records"
      border
      stripe
      v-loading="loading"
      :row-class-name="rowClassName"
      style="width: 100%"
    >
      <template #empty>
        <span class="empty-text">暂无记录</span>
      </template>

      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="productId" label="商品ID" />
      <el-table-column prop="productName" label="商品名称" />
      <el-table-column prop="type" label="类型">
        <template #default="{ row }">
          <el-tag :type="row.type === 'STOCK_IN' ? 'success' : 'danger'">
            {{ row.type === 'STOCK_IN' ? '入库' : '出库' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="quantity" label="数量" />
      <el-table-column prop="customerName" label="客户/操作员" />
      <el-table-column label="时间">
        <template #default="{ row }">
          {{ formatTime(row.createTime) }}
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrapper">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :page-sizes="[10, 20, 50, 100]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @current-change="fetchRecords"
        @size-change="handleSizeChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getStockRecords } from '@/api'
import { Search, Refresh } from '@element-plus/icons-vue'

const filters = reactive({
  type: '',
  productId: null
})

const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

const records = ref([])
const loading = ref(false)

async function fetchRecords() {
  loading.value = true
  try {
    const params = {
      type: filters.type || undefined,
      productId: filters.productId || undefined,
      page: pagination.page,
      size: pagination.size
    }
    const res = await getStockRecords(params)
    records.value = res.data.records || []
    pagination.total = res.data.total || 0
  } catch {
    records.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.page = 1
  fetchRecords()
}

function handleReset() {
  filters.type = ''
  filters.productId = null
  pagination.page = 1
  fetchRecords()
}

function handleSizeChange() {
  pagination.page = 1
  fetchRecords()
}

function rowClassName({ row }) {
  if (row.type === 'STOCK_IN') return 'row-in'
  if (row.type === 'STOCK_OUT') return 'row-out'
  return ''
}

function formatTime(time) {
  if (!time) return '-'
  const d = new Date(time)
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

onMounted(() => {
  fetchRecords()
})
</script>

<style scoped>
.admin-records {
  padding: 20px;
}

.filter-bar {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.empty-text {
  color: #909399;
  font-size: 14px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

:deep(.row-in) {
  background-color: #f0f9eb !important;
}
:deep(.row-out) {
  background-color: #fef0eb !important;
}
</style>
