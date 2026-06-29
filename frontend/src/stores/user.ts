import { defineStore } from 'pinia';
import { computed, ref } from 'vue';
import { fetchCurrentUser, login, logout, type UserInfo } from '@/api/auth';

const TOKEN_KEY = 'access_token';
const USER_KEY = 'user_info';

function readStoredUser(): UserInfo | null {
  const raw = localStorage.getItem(USER_KEY);
  if (!raw) {
    return null;
  }
  try {
    return JSON.parse(raw) as UserInfo;
  } catch {
    localStorage.removeItem(USER_KEY);
    return null;
  }
}

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem(TOKEN_KEY) || '');
  const userInfo = ref<UserInfo | null>(readStoredUser());
  const isAuthenticated = computed(() => Boolean(token.value));

  async function loginWithPassword(username: string, password: string) {
    const result = await login(username, password);
    token.value = result.accessToken;
    userInfo.value = result.userInfo;
    localStorage.setItem(TOKEN_KEY, result.accessToken);
    localStorage.setItem(USER_KEY, JSON.stringify(result.userInfo));
  }

  async function loadCurrentUser() {
    if (!token.value) {
      return;
    }
    const user = await fetchCurrentUser();
    userInfo.value = user;
    localStorage.setItem(USER_KEY, JSON.stringify(user));
  }

  async function logoutUser() {
    if (token.value) {
      try {
        await logout();
      } catch {
        // Local logout must still complete when the server is unreachable.
      }
    }
    clearSession();
  }

  function clearSession() {
    token.value = '';
    userInfo.value = null;
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
  }

  return {
    token,
    userInfo,
    isAuthenticated,
    loginWithPassword,
    loadCurrentUser,
    logoutUser,
    clearSession,
  };
});

