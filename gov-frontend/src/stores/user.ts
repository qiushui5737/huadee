import { computed, ref } from 'vue'
import { defineStore } from 'pinia'

export interface UserProfile {
  id?: number
  username?: string
  realName?: string
  gender?: string
  idCard?: string
  phone?: string
  email?: string
  deptCode?: string
  address?: string
  roles?: string[]
}

export const useUserStore = defineStore('user', () => {
  const token = ref('')
  const profile = ref<UserProfile>({})
  const username = computed(() => profile.value.realName || profile.value.username || '')
  const roles = computed(() => profile.value.roles || [])
  const isAdmin = computed(() => roles.value.includes('ADMIN'))
  const isLoggedIn = computed(() => Boolean(token.value))

  function setSession(t: string, data: UserProfile) {
    const admin = (data.roles || []).includes('ADMIN')
    token.value = t
    profile.value = data
    localStorage.setItem(admin ? 'admin_token' : 'c_token', t)
  }

  function setUser(data: UserProfile) {
    profile.value = { ...profile.value, ...data }
  }

  async function restoreSession(audience: 'admin' | 'portal' = 'portal') {
    const savedToken = localStorage.getItem(audience === 'admin' ? 'admin_token' : 'c_token')
    if (!savedToken) throw new Error('未登录')
    token.value = savedToken
    const { adminInfo } = await import('@/api/admin')
    const res: any = await adminInfo()
    profile.value = res.data
    if (audience === 'admin' && !isAdmin.value) throw new Error('无管理权限')
  }

  function clearSession(audience: 'admin' | 'portal' = 'portal') {
    localStorage.removeItem(audience === 'admin' ? 'admin_token' : 'c_token')
    token.value = ''
    profile.value = {}
  }

  return { token, profile, username, roles, isAdmin, isLoggedIn, setSession, setUser, restoreSession, clearSession }
})
