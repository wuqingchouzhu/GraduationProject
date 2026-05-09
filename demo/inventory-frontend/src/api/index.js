import request from '@/utils/request'

export const login = (data) => request.post('/api/auth/login', data)
export const verifyPassword = (data) => request.post('/api/auth/verify-password', data)

export const getProductList = () => request.get('/api/product/list')
export const stockIn = (data) => request.post('/api/product/stockIn', data)
export const stockOut = (data) => request.post('/api/product/stockOut', data)
export const addProduct = (data) => request.post('/api/product/add', data)
export const deleteProduct = (id) => request.delete(`/api/product/delete/${id}`)
export const updateProduct = (id, data) => request.put(`/api/product/update/${id}`, data)

export const getUsers = () => request.get('/api/admin/users')
export const addUser = (data) => request.post('/api/admin/users', data)
export const deleteUser = (id, data) => request.delete(`/api/admin/users/${id}`, { data })

export const getStockRecords = (params) => request.get('/api/admin/stock-records', { params })

export const getDashboard = () => request.get('/api/admin/dashboard')

export const getFailedMessages = () => request.get('/api/admin/failed-messages')
export const retryFailedMessage = (id) => request.post(`/api/admin/retry/${id}`)
