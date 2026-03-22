<template>
  <div class="common-layout">
    <div v-if="!isLoggedIn" class="login-container">
      <el-card class="login-card">
        <h2 style="text-align: center; margin-bottom: 20px;">进销存系统登录</h2>
        <el-form :model="loginForm" label-width="60px">
          <el-form-item label="账号"><el-input v-model="loginForm.username" /></el-form-item>
          <el-form-item label="密码"><el-input v-model="loginForm.password" type="password" show-password /></el-form-item>
          <el-button type="primary" style="width: 100%; margin-top: 10px;" @click="handleLogin" :loading="loginLoading">登录</el-button>
        </el-form>
      </el-card>
    </div>

    <el-container v-else style="height: 100vh;">
      <el-aside width="200px" style="background-color: #304156; color: white;">
        <h3 style="text-align: center; margin-top: 20px;">进销存系统</h3>
        <el-menu background-color="#304156" text-color="#fff" active-text-color="#409EFF" default-active="1">
          <el-menu-item index="1">库存管理</el-menu-item>
        </el-menu>
      </el-aside>

      <el-container>
        <el-header style="border-bottom: 1px solid #ddd; display: flex; align-items: center; justify-content: space-between;">
          <span>当前用户：{{ currentUser.username }} ({{ currentUser.role === 'ADMIN' ? '管理员' : '员工' }})</span>
          <el-button type="danger" size="small" @click="handleLogout">退出登录</el-button>
        </el-header>

        <el-main>
          <div style="margin-bottom: 20px;">
            <el-button type="primary" @click="fetchData">刷新数据</el-button>
            <el-button v-if="currentUser.role === 'ADMIN'" type="success" @click="openAddDialog">新增商品</el-button>
          </div>

          <el-table :data="productList" border style="width: 100%" v-loading="loading">
            <el-table-column prop="id" label="ID" width="60" />
            <el-table-column prop="productCode" label="商品编号" />
            <el-table-column prop="productName" label="商品名称" />
            <el-table-column prop="stockLevel" label="当前库存">
              <template #default="scope">
                <el-tag :type="scope.row.stockLevel < 20 ? 'danger' : 'success'">{{ scope.row.stockLevel }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="unitPrice" label="单价" />
            <el-table-column label="操作" width="250">
              <template #default="scope">
                <el-button size="small" type="primary" @click="handleAction(scope.row, 'IN')">入库</el-button>
                <el-button size="small" type="warning" @click="handleAction(scope.row, 'OUT')">出库</el-button>
                <el-button v-if="currentUser.role === 'ADMIN'" size="small" type="danger" @click="handleDelete(scope.row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-main>
      </el-container>
    </el-container>

    <el-dialog v-model="showStockDialog" :title="actionType === 'IN' ? '入库' : '出库'" width="30%">
      <el-form label-width="80px">
        <el-form-item label="商品"><el-input v-model="selectedItem.productName" disabled /></el-form-item>
        <el-form-item label="数量"><el-input-number v-model="formAmount" :min="1" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showStockDialog = false">取消</el-button>
        <el-button type="primary" @click="submitStock">提交</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showAddDialog" title="新增商品" width="30%">
      <el-form label-width="80px">
        <el-form-item label="编号"><el-input v-model="newProduct.productCode" /></el-form-item>
        <el-form-item label="名称"><el-input v-model="newProduct.productName" /></el-form-item>
        <el-form-item label="单价"><el-input-number v-model="newProduct.unitPrice" :min="0" :precision="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="success" @click="submitAdd">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'

const isLoggedIn = ref(false)
const currentUser = ref({ username: '', role: '' })
const loginForm = ref({ username: '', password: '' })
const loginLoading = ref(false)
const productList = ref([])
const loading = ref(false)
const showStockDialog = ref(false)
const actionType = ref('IN')
const selectedItem = ref({})
const formAmount = ref(1)
const showAddDialog = ref(false)
const newProduct = ref({ productCode: '', productName: '', unitPrice: 0 })

const handleLogin = async () => {
  loginLoading.value = true
  try {
    const res = await axios.post('http://localhost:8080/api/auth/login', loginForm.value)
    if (res.data.code === 200) {
      isLoggedIn.value = true
      currentUser.value = res.data.data
      fetchData()
    } else { ElMessage.error(res.data.message) }
  } catch (err) { ElMessage.error('登录请求失败') }
  finally { loginLoading.value = false }
}

const handleLogout = () => {
  isLoggedIn.value = false
  currentUser.value = { username: '', role: '' }
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await axios.get('http://localhost:8080/api/product/list')
    productList.value = res.data.data
  } catch (err) { ElMessage.error('获取数据失败') }
  finally { loading.value = false }
}

const handleAction = (row, type) => {
  selectedItem.value = { ...row }; actionType.value = type; formAmount.value = 1; showStockDialog.value = true
}

const submitStock = async () => {
  const path = actionType.value === 'IN' ? 'stockIn' : 'stockOut'
  try {
    const res = await axios.post(`http://localhost:8080/api/product/${path}`, { id: selectedItem.value.id, quantity: formAmount.value })
    if (res.data.code === 200) { showStockDialog.value = false; fetchData() }
    else { ElMessage.error(res.data.message) }
  } catch (err) { ElMessage.error('网络错误') }
}

const openAddDialog = () => {
  newProduct.value = { productCode: '', productName: '', unitPrice: 0 }; showAddDialog.value = true
}

const submitAdd = async () => {
  try {
    const res = await axios.post('http://localhost:8080/api/product/add', newProduct.value)
    if (res.data.code === 200) { showAddDialog.value = false; fetchData() }
    else { ElMessage.error(res.data.message) }
  } catch (err) { ElMessage.error('新增失败') }
}

// 删除逻辑：带二次确认
const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定要彻底删除商品 [${row.productName}] 吗？此操作不可恢复！`,
    '警告',
    {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(async () => {
    try {
      const res = await axios.delete(`http://localhost:8080/api/product/delete/${row.id}`)
      if (res.data.code === 200) {
        ElMessage.success('删除成功')
        fetchData()
      } else {
        ElMessage.error(res.data.message)
      }
    } catch (err) {
      ElMessage.error('删除请求失败')
    }
  }).catch(() => {
    // 点击取消不做处理
  })
}
</script>

<style>
body { margin: 0; background-color: #f0f2f5; }
.login-container { display: flex; justify-content: center; align-items: center; height: 100vh; }
.login-card { width: 350px; }
</style>