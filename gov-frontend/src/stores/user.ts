import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(localStorage.getItem('admin_token') || '')
  const username = ref<string>('')
  const roles = ref<string[]>([])

  function setToken(t: string) {
    token.value = t
    localStorage.setItem('admin_token', t)
  }

  function setUser(name: string, r: string[]) {
    username.value = name
    roles.value = r
  }

  async function restoreSession() {
    if (!token.value || username.value) return
    const { adminInfo } = await import('@/api/admin')
    const res: any = await adminInfo()
    setUser(res.data.realName || res.data.username, res.data.roles || [])
  }

  function logout() {
    token.value = ''
    username.value = ''
    roles.value = []
    localStorage.removeItem('admin_token')
  }

  return { token, username, roles, setToken, setUser, restoreSession, logout }
})
