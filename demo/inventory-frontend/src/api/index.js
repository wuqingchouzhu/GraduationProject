import request from '@/utils/request'

// ===== 认证 =====
export const login = (data) => request.post('/api/auth/login', data)
export const verifyPassword = (data) => request.post('/api/auth/verify-password', data)

// ===== 商品 =====
export const getProductList = () => request.get('/api/product/list')
export const stockIn = (data) => request.post('/api/product/stockIn', data)
export const stockOut = (data) => request.post('/api/product/stockOut', data)
export const addProduct = (data) => request.post('/api/product/add', data)
export const deleteProduct = (id) => request.delete(`/api/product/delete/${id}`)
export const updateProduct = (id, data) => request.put(`/api/product/update/${id}`, data)

// ===== 管理员 - 用户管理 =====
export const getUsers = () => request.get('/api/admin/users')
export const addUser = (data) => request.post('/api/admin/users', data)
export const deleteUser = (id, data) => request.delete(`/api/admin/users/${id}`, { data })

// ===== 管理员 - 出入库记录 =====
export const getStockRecords = (params) => request.get('/api/admin/stock-records', { params })

// ===== 仪表盘 =====
export const getDashboard = () => request.get('/api/admin/dashboard')

// ===== 管理员 - 失败消息 =====
export const getFailedMessages = () => request.get('/api/admin/failed-messages')
export const retryFailedMessage = (id) => request.post(`/api/admin/retry/${id}`)
