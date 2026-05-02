<template>
  <div class="admin-users">
    <!-- Toolbar -->
    <div class="toolbar">
      <el-button type="primary" :icon="Plus" @click="openAddDialog">
        新增用户
      </el-button>
    </div>

    <!-- User Table -->
    <el-table
      :data="users"
      border
      stripe
      v-loading="loading"
      style="width: 100%"
    >
      <template #empty>
        <span class="empty-text">暂无用户</span>
      </template>

      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="用户名" />
      <el-table-column label="角色">
        <template #default="{ row }">
          <el-tag :type="row.role === 'ADMIN' ? 'danger' : 'info'">
            {{ row.role === 'ADMIN' ? '管理员' : '普通用户' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button
            type="danger"
            size="small"
            text
            :disabled="row.username === 'admin'"
            @click="openDeleteDialog(row)"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- Add User Dialog -->
    <el-dialog
      v-model="showAddDialog"
      title="新增用户"
      width="450px"
      destroy-on-close
    >
      <el-form
        ref="addFormRef"
        :model="addForm"
        :rules="addRules"
        label-width="80px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="addForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="addForm.password"
            type="password"
            show-password
            placeholder="请输入密码"
          />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="addForm.role" style="width: 100%">
            <el-option label="普通用户" value="USER" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
        </el-form-item>

        <el-divider content-position="left">安全验证</el-divider>

        <el-form-item label="管理员密码" prop="adminPassword">
          <el-input
            v-model="addForm.adminPassword"
            type="password"
            show-password
            placeholder="请输入管理员密码以确认操作"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" :loading="addLoading" @click="handleAddUser">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- Delete User Password Dialog -->
    <el-dialog
      v-model="showDeleteDialog"
      title="安全验证"
      width="380px"
      destroy-on-close
    >
      <p class="delete-warning">
        删除用户「{{ deleteTarget?.username }}」需要验证管理员密码
      </p>
      <el-form
        ref="deleteFormRef"
        :model="deleteForm"
        :rules="deleteRules"
        label-width="80px"
      >
        <el-form-item label="管理员密码" prop="adminPassword">
          <el-input
            v-model="deleteForm.adminPassword"
            type="password"
            show-password
            placeholder="请输入管理员密码"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDeleteDialog = false">取消</el-button>
        <el-button
          type="danger"
          :loading="deleteLoading"
          @click="handleDeleteUser"
        >
          确认删除
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getUsers, addUser, deleteUser } from '@/api'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

// ----- Table data -----
const users = ref([])
const loading = ref(false)

// ----- Add User -----
const showAddDialog = ref(false)
const addFormRef = ref(null)
const addLoading = ref(false)

const addForm = reactive({
  username: '',
  password: '',
  role: 'USER',
  adminPassword: ''
})

const addRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, message: '用户名至少2个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6个字符', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ],
  adminPassword: [
    { required: true, message: '请输入管理员密码', trigger: 'blur' }
  ]
}

// ----- Delete User -----
const showDeleteDialog = ref(false)
const deleteFormRef = ref(null)
const deleteLoading = ref(false)
const deleteTarget = ref(null)

const deleteForm = reactive({
  adminPassword: ''
})

const deleteRules = {
  adminPassword: [
    { required: true, message: '请输入管理员密码', trigger: 'blur' }
  ]
}

// ----- Methods -----
async function fetchUsers() {
  loading.value = true
  try {
    const res = await getUsers()
    users.value = res.data || []
  } catch {
    users.value = []
  } finally {
    loading.value = false
  }
}

function openAddDialog() {
  addForm.username = ''
  addForm.password = ''
  addForm.role = 'USER'
  addForm.adminPassword = ''
  addFormRef.value?.resetFields()
  showAddDialog.value = true
}

async function handleAddUser() {
  const valid = await addFormRef.value.validate().catch(() => false)
  if (!valid) return

  addLoading.value = true
  try {
    await addUser({
      username: addForm.username,
      password: addForm.password,
      role: addForm.role,
      adminPassword: addForm.adminPassword
    })
    ElMessage.success('新增用户成功')
    showAddDialog.value = false
    fetchUsers()
  } catch {
    // Interceptor already shows ElMessage.error for failures
  } finally {
    addLoading.value = false
  }
}

function openDeleteDialog(row) {
  deleteTarget.value = row
  deleteForm.adminPassword = ''
  deleteFormRef.value?.resetFields()
  showDeleteDialog.value = true
}

async function handleDeleteUser() {
  const valid = await deleteFormRef.value.validate().catch(() => false)
  if (!valid) return

  deleteLoading.value = true
  try {
    await deleteUser(deleteTarget.value.id, {
      adminPassword: deleteForm.adminPassword
    })
    ElMessage.success('删除用户成功')
    showDeleteDialog.value = false
    fetchUsers()
  } catch (err) {
    if (err.message && err.message.includes('不能删除自己')) {
      ElMessage.warning('不能删除自己')
    }
    // Other errors handled by interceptor
  } finally {
    deleteLoading.value = false
  }
}

// ----- Lifecycle -----
onMounted(() => {
  fetchUsers()
})
</script>

<style scoped>
.admin-users {
  padding: 20px;
}

.toolbar {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 16px;
}

.empty-text {
  color: #909399;
  font-size: 14px;
}

.delete-warning {
  margin-bottom: 16px;
  color: #e6a23c;
  font-size: 14px;
}
</style>
