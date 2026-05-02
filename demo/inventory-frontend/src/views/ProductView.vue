<template>
  <div class="product-page">
    <!-- Toolbar -->
    <div class="toolbar">
      <div class="toolbar-left">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索商品编号/名称"
          clearable
          :prefix-icon="Search"
          class="search-input"
        />
        <el-button :icon="Refresh" @click="fetchProducts">刷新数据</el-button>
      </div>
      <div class="toolbar-right">
        <el-button v-if="auth.isAdmin" type="success" :icon="Plus" @click="openAddDialog">
          新增商品
        </el-button>
      </div>
    </div>

    <!-- Product Table -->
    <el-table
      v-loading="loading"
      :data="pagedData"
      border
      stripe
      :row-class-name="rowClassName"
      class="product-table"
      style="width: 100%"
      @sort-change="handleSortChange"
    >
      <el-table-column prop="id" label="ID" width="60" align="center" sortable="custom" />
      <el-table-column prop="productCode" label="商品编号" width="130" sortable="custom" />
      <el-table-column prop="productName" label="商品名称" min-width="140" show-overflow-tooltip sortable="custom" />

      <el-table-column label="预警状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="row.alertStatus === 1 ? 'danger' : 'success'" size="small" effect="dark">
            {{ row.alertStatus === 1 ? '预警中' : '正常' }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column prop="stockLevel" label="当前库存" width="140" align="center" sortable="custom">
        <template #default="{ row }">
          <div class="stock-cell">
            <el-tag :type="stockTagType(row)" size="small" effect="dark">
              {{ row.stockLevel }}
            </el-tag>
            <span v-if="row.reorderPoint > 0" class="reorder-hint">
              预警点 {{ formatNumber(row.reorderPoint) }}
            </span>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="单价" width="100" align="right">
        <template #default="{ row }">
          <span class="price-cell">{{ formatPrice(row.unitPrice) }}</span>
        </template>
      </el-table-column>

      <el-table-column label="操作" width="210" align="center" fixed="right">
        <template #default="{ row }">
          <div class="action-btns">
            <el-button type="primary" size="small" @click="openStockIn(row)">
              入库
            </el-button>
            <el-button type="warning" size="small" @click="openStockOut(row)">
              出库
            </el-button>
            <el-button
              v-if="auth.isAdmin"
              type="primary"
              size="small"
              :icon="Edit"
              @click="openEditDialog(row)"
            />
            <el-button
              v-if="auth.isAdmin"
              type="danger"
              size="small"
              :icon="Delete"
              @click="handleDelete(row)"
            />
          </div>
        </template>
      </el-table-column>
    </el-table>

    <!-- Pagination -->
    <div class="pagination-wrap">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[5, 10, 20, 50]"
        :total="filteredData.length"
        layout="total, sizes, prev, pager, next, jumper"
        background
      />
    </div>

    <!-- Stock In Dialog -->
    <el-dialog
      v-model="stockInVisible"
      title="商品入库"
      width="420px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <el-form :model="stockForm" label-width="80px">
        <el-form-item label="商品名称">
          <el-input :model-value="currentProduct?.productName" readonly />
        </el-form-item>
        <el-form-item label="商品编号">
          <el-input :model-value="currentProduct?.productCode" readonly />
        </el-form-item>
        <el-form-item label="入库数量" required>
          <el-input-number
            v-model="stockForm.quantity"
            :min="1"
            :max="99999"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="stockInVisible = false">取消</el-button>
        <el-button type="primary" :loading="stockLoading" @click="submitStockIn">
          确认入库
        </el-button>
      </template>
    </el-dialog>

    <!-- Stock Out Dialog -->
    <el-dialog
      v-model="stockOutVisible"
      title="商品出库"
      width="420px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <el-form :model="stockForm" label-width="80px">
        <el-form-item label="商品名称">
          <el-input :model-value="currentProduct?.productName" readonly />
        </el-form-item>
        <el-form-item label="商品编号">
          <el-input :model-value="currentProduct?.productCode" readonly />
        </el-form-item>
        <el-form-item label="出库数量" required>
          <el-input-number
            v-model="stockForm.quantity"
            :min="1"
            :max="currentProduct?.stockLevel || 1"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="stockOutVisible = false">取消</el-button>
        <el-button type="primary" :loading="stockLoading" @click="submitStockOut">
          确认出库
        </el-button>
      </template>
    </el-dialog>

    <!-- Add Product Dialog -->
    <el-dialog
      v-if="auth.isAdmin"
      v-model="addVisible"
      title="新增商品"
      width="450px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <el-form
        ref="addFormRef"
        :model="addForm"
        :rules="addRules"
        label-width="80px"
      >
        <el-form-item label="商品编号" prop="productCode">
          <el-input v-model="addForm.productCode" placeholder="如 P001" />
        </el-form-item>
        <el-form-item label="商品名称" prop="productName">
          <el-input v-model="addForm.productName" placeholder="请输入商品名称" />
        </el-form-item>
        <el-form-item label="单价" prop="unitPrice">
          <el-input-number
            v-model="addForm.unitPrice"
            :min="0"
            :precision="2"
            :step="0.01"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addVisible = false">取消</el-button>
        <el-button type="primary" :loading="addLoading" @click="submitAdd">
          确认新增
        </el-button>
      </template>
    </el-dialog>

    <!-- Edit Product Dialog -->
    <el-dialog
      v-model="editVisible"
      title="编辑商品"
      width="450px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <el-form
        ref="editFormRef"
        :model="editForm"
        :rules="editRules"
        label-width="80px"
      >
        <el-form-item label="商品编号" prop="productCode">
          <el-input v-model="editForm.productCode" placeholder="如 P001" />
        </el-form-item>
        <el-form-item label="商品名称" prop="productName">
          <el-input v-model="editForm.productName" placeholder="请输入商品名称" />
        </el-form-item>
        <el-form-item label="单价" prop="unitPrice">
          <el-input-number
            v-model="editForm.unitPrice"
            :min="0"
            :precision="2"
            :step="0.01"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="补货天数" prop="leadTime">
          <el-input-number
            v-model="editForm.leadTime"
            :min="1"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="editLoading" @click="submitEdit">
          确认修改
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { Search, Refresh, Plus, Delete, Edit } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getProductList, stockIn, stockOut, addProduct, deleteProduct, updateProduct } from '@/api'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()

// ── Data ──────────────────────────────────────────
const loading = ref(false)
const products = ref([])
const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const sortState = ref({ prop: '', order: '' })

// ── Computed ──────────────────────────────────────
const filteredData = computed(() => {
  const kw = searchKeyword.value.trim().toLowerCase()
  if (!kw) return products.value
  return products.value.filter(
    (p) =>
      (p.productCode && p.productCode.toLowerCase().includes(kw)) ||
      (p.productName && p.productName.toLowerCase().includes(kw))
  )
})

const pagedData = computed(() => {
  let sorted = filteredData.value
  if (sortState.value.prop) {
    sorted = [...filteredData.value].sort((a, b) => {
      const va = a[sortState.value.prop]
      const vb = b[sortState.value.prop]
      if (typeof va === 'number') return sortState.value.order === 'ascending' ? va - vb : vb - va
      if (typeof va === 'string') return sortState.value.order === 'ascending' ? va.localeCompare(vb) : vb.localeCompare(va)
      return 0
    })
  }
  const start = (currentPage.value - 1) * pageSize.value
  return sorted.slice(start, start + pageSize.value)
})

// ── Fetch ─────────────────────────────────────────
async function fetchProducts() {
  loading.value = true
  try {
    const res = await getProductList()
    products.value = res.data || []
  } finally {
    loading.value = false
  }
}

// ── Sort ─────────────────────────────────────────
function handleSortChange({ prop, order }) {
  sortState.value.prop = prop || ''
  sortState.value.order = order || ''
  currentPage.value = 1
}

// ── Row class ─────────────────────────────────────
function rowClassName({ row }) {
  return row.alertStatus === 1 ? 'row-alert' : ''
}

// ── Stock tag ─────────────────────────────────────
function stockTagType(row) {
  if (row.alertStatus === 1) return 'danger'
  if (row.stockLevel < 20) return 'warning'
  if (row.stockLevel === 0) return 'info'
  return 'success'
}

// ── Format ────────────────────────────────────────
function formatPrice(val) {
  if (val === null || val === undefined) return '¥0.00'
  const n = Number(val)
  return '¥' + n.toFixed(2)
}

function formatNumber(val) {
  if (val === null || val === undefined) return '0'
  return Number(val).toFixed(1)
}

// ── Stock In ──────────────────────────────────────
const stockInVisible = ref(false)
const stockOutVisible = ref(false)
const stockLoading = ref(false)
const currentProduct = ref(null)
const stockForm = reactive({ quantity: 1 })

function openStockIn(row) {
  currentProduct.value = row
  stockForm.quantity = 1
  stockInVisible.value = true
}

async function submitStockIn() {
  if (!currentProduct.value) return
  stockLoading.value = true
  try {
    await stockIn({
      productId: currentProduct.value.id,
      quantity: stockForm.quantity
    })
    ElMessage.success('入库请求已提交')
    stockInVisible.value = false
    await fetchProducts()
  } finally {
    stockLoading.value = false
  }
}

// ── Stock Out ──────────────────────────────────────
function openStockOut(row) {
  currentProduct.value = row
  stockForm.quantity = 1
  stockOutVisible.value = true
}

async function submitStockOut() {
  if (!currentProduct.value) return
  stockLoading.value = true
  try {
    await stockOut({
      productId: currentProduct.value.id,
      quantity: stockForm.quantity
    })
    ElMessage.success('出库请求已提交')
    stockOutVisible.value = false
    await fetchProducts()
  } finally {
    stockLoading.value = false
  }
}

// ── Add Product ───────────────────────────────────
const addVisible = ref(false)
const addLoading = ref(false)
const addFormRef = ref(null)

const addForm = reactive({
  productCode: '',
  productName: '',
  unitPrice: 0
})

const addRules = {
  productCode: [{ required: true, message: '请输入商品编号', trigger: 'blur' }],
  productName: [{ required: true, message: '请输入商品名称', trigger: 'blur' }]
}

function openAddDialog() {
  addForm.productCode = ''
  addForm.productName = ''
  addForm.unitPrice = 0
  addFormRef.value?.resetFields()
  addVisible.value = true
}

async function submitAdd() {
  const valid = await addFormRef.value.validate().catch(() => false)
  if (!valid) return
  addLoading.value = true
  try {
    await addProduct({
      productCode: addForm.productCode,
      productName: addForm.productName,
      unitPrice: addForm.unitPrice
    })
    ElMessage.success('商品添加成功')
    addVisible.value = false
    await fetchProducts()
  } finally {
    addLoading.value = false
  }
}

// ── Delete ────────────────────────────────────────
async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(
      `确定要删除商品「${row.productName}」(${row.productCode}) 吗？此操作不可恢复。`,
      '删除确认',
      { confirmButtonText: '确认删除', cancelButtonText: '取消', type: 'warning' }
    )
  } catch {
    return // user cancelled
  }
  try {
    await deleteProduct(row.id)
    ElMessage.success('删除成功')
    await fetchProducts()
  } catch {
    // error already shown by interceptor
  }
}

// ── Edit Product ─────────────────────────────────
const editVisible = ref(false)
const editLoading = ref(false)
const editFormRef = ref(null)
const editingProduct = ref(null)

const editForm = reactive({
  productCode: '',
  productName: '',
  unitPrice: 0,
  leadTime: 1
})

const editRules = {
  productCode: [{ required: true, message: '请输入商品编号', trigger: 'blur' }],
  productName: [{ required: true, message: '请输入商品名称', trigger: 'blur' }]
}

function openEditDialog(row) {
  editingProduct.value = row
  editForm.productCode = row.productCode
  editForm.productName = row.productName
  editForm.unitPrice = row.unitPrice
  editForm.leadTime = row.leadTime || 1
  editFormRef.value?.resetFields()
  editVisible.value = true
}

async function submitEdit() {
  const valid = await editFormRef.value.validate().catch(() => false)
  if (!valid) return
  editLoading.value = true
  try {
    await updateProduct(editingProduct.value.id, {
      productCode: editForm.productCode,
      productName: editForm.productName,
      unitPrice: editForm.unitPrice,
      leadTime: editForm.leadTime
    })
    ElMessage.success('商品修改成功')
    editVisible.value = false
    await fetchProducts()
  } finally {
    editLoading.value = false
  }
}

// ── Init ──────────────────────────────────────────
onMounted(fetchProducts)
</script>

<style scoped>
.product-page {
  padding: 20px;
}

/* ── Toolbar ─────────────────────── */
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 1;
}

.toolbar-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.search-input {
  width: 260px;
}

/* ── Table ───────────────────────── */
.product-table {
  border-radius: 6px;
  overflow: hidden;
}

/* Alert row highlight */
:deep(.row-alert) {
  background-color: #fef0f0 !important;
}
:deep(.row-alert:hover > td) {
  background-color: #fde2e2 !important;
}

/* Stock cell */
.stock-cell {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
}

.reorder-hint {
  font-size: 11px;
  color: #909399;
}

/* Price cell */
.price-cell {
  font-variant-numeric: tabular-nums;
  font-weight: 500;
  color: #303133;
}

/* Action buttons */
.action-btns {
  display: flex;
  gap: 4px;
  justify-content: center;
}

/* ── Pagination ──────────────────── */
.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
