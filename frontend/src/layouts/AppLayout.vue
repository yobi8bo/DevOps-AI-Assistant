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
        <a-menu-item key="/cases">
          <book-outlined />
          <span>案例库</span>
        </a-menu-item>
        <a-menu-item key="/knowledge">
          <read-outlined />
          <span>知识库</span>
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
        <a-menu-item key="/users">
          <team-outlined />
          <span>用户管理</span>
        </a-menu-item>
        <a-menu-item key="/roles">
          <safety-certificate-outlined />
          <span>角色权限</span>
        </a-menu-item>
        <a-menu-item key="/fault-categories">
          <tags-outlined />
          <span>故障分类</span>
        </a-menu-item>
        <a-menu-item key="/operation-logs">
          <audit-outlined />
          <span>系统日志</span>
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
                <a-menu-item key="change-password" @click="openPasswordModal">
                  <lock-outlined />
                  <span>修改密码</span>
                </a-menu-item>
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

    <a-modal
      v-model:open="passwordOpen"
      title="修改密码"
      :confirm-loading="passwordSaving"
      @ok="submitChangePassword"
    >
      <a-form layout="vertical">
        <a-form-item label="旧密码" required>
          <a-input-password v-model:value="passwordForm.oldPassword" />
        </a-form-item>
        <a-form-item label="新密码" required>
          <a-input-password v-model:value="passwordForm.newPassword" />
        </a-form-item>
        <a-form-item label="确认新密码" required>
          <a-input-password v-model:value="passwordForm.confirmPassword" />
        </a-form-item>
      </a-form>
    </a-modal>
  </a-layout>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue';
import { message } from 'ant-design-vue';
import { useRoute, useRouter } from 'vue-router';
import { AuditOutlined, BookOutlined, DashboardOutlined, FileTextOutlined, HistoryOutlined, LockOutlined, LogoutOutlined, ProfileOutlined, ReadOutlined, SafetyCertificateOutlined, SettingOutlined, TagsOutlined, TeamOutlined, ThunderboltOutlined, UserOutlined } from '@ant-design/icons-vue';
import { changePassword } from '@/api/auth';
import { useUserStore } from '@/stores/user';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();
const passwordOpen = ref(false);
const passwordSaving = ref(false);
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
});

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

function openPasswordModal() {
  Object.assign(passwordForm, {
    oldPassword: '',
    newPassword: '',
    confirmPassword: '',
  });
  passwordOpen.value = true;
}

async function submitChangePassword() {
  if (!passwordForm.oldPassword || !passwordForm.newPassword) {
    message.warning('请填写旧密码和新密码');
    return;
  }
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    message.warning('两次输入的新密码不一致');
    return;
  }
  passwordSaving.value = true;
  try {
    await changePassword(passwordForm.oldPassword, passwordForm.newPassword);
    message.success('密码已修改，请重新登录');
    passwordOpen.value = false;
    userStore.clearSession();
    router.replace('/login');
  } catch (error) {
    message.error(error instanceof Error ? error.message : '修改密码失败');
  } finally {
    passwordSaving.value = false;
  }
}
</script>
