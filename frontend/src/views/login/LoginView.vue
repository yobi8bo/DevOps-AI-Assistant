<template>
  <div class="login-page">
    <div class="login-panel">
      <div class="login-brand">
        <div class="brand-mark">AI</div>
        <div>
          <h1>AI 运维排障助手</h1>
          <p>登录后开始分析日志、命令输出和故障现象。</p>
        </div>
      </div>

      <a-form layout="vertical" class="login-form" @submit.prevent="submitLogin">
        <a-form-item label="用户名">
          <a-input v-model:value="form.username" size="large" autocomplete="username" @pressEnter="submitLogin">
            <template #prefix>
              <user-outlined />
            </template>
          </a-input>
        </a-form-item>
        <a-form-item label="密码">
          <a-input-password v-model:value="form.password" size="large" autocomplete="current-password" @pressEnter="submitLogin">
            <template #prefix>
              <lock-outlined />
            </template>
          </a-input-password>
        </a-form-item>
        <a-button type="primary" size="large" block :loading="loading" @click="submitLogin">
          登录
        </a-button>
      </a-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import { message } from 'ant-design-vue';
import { LockOutlined, UserOutlined } from '@ant-design/icons-vue';
import { useRoute, useRouter } from 'vue-router';
import { useUserStore } from '@/stores/user';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();
const loading = ref(false);

const form = reactive({
  username: 'admin',
  password: 'admin123456',
});

async function submitLogin() {
  if (!form.username || !form.password) {
    message.warning('请输入用户名和密码');
    return;
  }
  loading.value = true;
  try {
    await userStore.loginWithPassword(form.username, form.password);
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/';
    router.replace(redirect);
  } catch (error) {
    message.error(error instanceof Error ? error.message : '登录失败');
  } finally {
    loading.value = false;
  }
}
</script>

