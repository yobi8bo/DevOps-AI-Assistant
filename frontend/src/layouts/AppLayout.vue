<template>
  <a-layout class="app-shell">
    <a-layout-sider class="app-sider" width="232">
      <div class="brand">
        <div class="brand-mark">AI</div>
        <div>
          <div class="brand-title">AI 运维排障助手</div>
          <div class="brand-subtitle">DevOps Diagnosis</div>
        </div>
      </div>
      <a-menu v-model:selectedKeys="selectedKeys" theme="dark" mode="inline">
        <a-menu-item key="/">
          <dashboard-outlined />
          <span>工作台</span>
        </a-menu-item>
        <a-menu-item key="/diagnosis">
          <thunderbolt-outlined />
          <span>智能排障</span>
        </a-menu-item>
        <a-menu-item key="/history">
          <history-outlined />
          <span>排障历史</span>
        </a-menu-item>
        <a-menu-item key="/model-configs">
          <setting-outlined />
          <span>模型配置</span>
        </a-menu-item>
        <a-menu-item key="/prompt-templates">
          <file-text-outlined />
          <span>Prompt 模板</span>
        </a-menu-item>
        <a-menu-item key="/ai-call-logs">
          <profile-outlined />
          <span>AI 调用日志</span>
        </a-menu-item>
      </a-menu>
    </a-layout-sider>
    <a-layout>
      <a-layout-header class="app-header">
        <div class="header-title">粘贴报错日志，自动生成排查步骤</div>
        <a-space>
          <a-tag color="blue">MVP</a-tag>
          <a-dropdown>
            <a-button>
              <user-outlined />
              {{ userStore.userInfo?.nickname || userStore.userInfo?.username || '用户' }}
            </a-button>
            <template #overlay>
              <a-menu>
                <a-menu-item key="logout" @click="handleLogout">
                  <logout-outlined />
                  <span>退出登录</span>
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </a-space>
      </a-layout-header>
      <a-layout-content class="app-content">
        <router-view />
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { DashboardOutlined, FileTextOutlined, HistoryOutlined, LogoutOutlined, ProfileOutlined, SettingOutlined, ThunderboltOutlined, UserOutlined } from '@ant-design/icons-vue';
import { useUserStore } from '@/stores/user';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

const selectedKeys = computed({
  get: () => [route.path],
  set: ([path]) => {
    if (path && path !== route.path) {
      router.push(path);
    }
  },
});

async function handleLogout() {
  await userStore.logoutUser();
  router.replace('/login');
}
</script>
