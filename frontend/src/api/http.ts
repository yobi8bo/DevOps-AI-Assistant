import axios from 'axios';
import { message } from 'ant-design-vue';
import router from '@/router';

export interface ApiResponse<T> {
  code: string;
  message: string;
  data: T;
}

const SUCCESS_CODE = 'SUCCESS';
const AUTH_ERROR_CODES = new Set(['AUTH_UNAUTHORIZED', 'AUTH_FORBIDDEN']);

export const http = axios.create({
  baseURL: '/api',
  timeout: 60000,
});

http.interceptors.request.use((config) => {
  const token = localStorage.getItem('access_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

http.interceptors.response.use(
  (response) => {
    const body = response.data as ApiResponse<unknown>;
    if (body.code !== SUCCESS_CODE) {
      return Promise.reject(new Error(body.message || '请求失败'));
    }
    return response;
  },
  (error) => {
    const body = error.response?.data as Partial<ApiResponse<unknown>> | undefined;
    const errorCode = body?.code;
    const unauthorized =
      error.response?.status === 401 ||
      error.response?.status === 403 ||
      (errorCode != null && AUTH_ERROR_CODES.has(errorCode));

    if (unauthorized) {
      localStorage.removeItem('access_token');
      localStorage.removeItem('user_info');
      if (router.currentRoute.value.path !== '/login') {
        message.warning(body?.message || '登录已过期，请重新登录');
        router.replace({
          path: '/login',
          query: { redirect: router.currentRoute.value.fullPath },
        });
      }
    }
    return Promise.reject(new Error(body?.message || error.message || '请求失败'));
  },
);
