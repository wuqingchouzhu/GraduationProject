import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi } from '@/api'

export const useAuthStore = defineStore('auth', () => {
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))

  const isLoggedIn = computed(() => user.value !== null)
  const isAdmin = computed(() => user.value?.role === 'ADMIN')

  async function login(credentials) {
    const res = await loginApi(credentials)
    user.value = res.data
    localStorage.setItem('user', JSON.stringify(res.data))
    return res
  }

  function logout() {
    user.value = null
    localStorage.removeItem('user')
  }

  return { user, isLoggedIn, isAdmin, login, logout }
})
