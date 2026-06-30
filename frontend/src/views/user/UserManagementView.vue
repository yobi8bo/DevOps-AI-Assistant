<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h1>用户管理</h1>
        <p>维护登录账号、启停状态、密码和角色绑定。</p>
      </div>
      <a-space>
        <a-button @click="loadUsers">
          <reload-outlined />
          刷新
        </a-button>
        <a-button type="primary" @click="openCreate">
          <plus-outlined />
          新增用户
        </a-button>
      </a-space>
    </div>

    <a-card :bordered="false" class="filter-card">
      <a-form layout="inline">
        <a-form-item label="关键词">
          <a-input v-model:value="query.keyword" allow-clear placeholder="用户名、昵称、邮箱或手机号" @pressEnter="handleSearch" />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="query.status" allow-clear :options="statusOptions" class="filter-select" />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleSearch">
              <search-outlined />
              查询
            </a-button>
            <a-button @click="resetSearch">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-card :bordered="false" class="section">
      <a-table
        row-key="id"
        :columns="columns"
        :data-source="users"
        :loading="loading"
        :pagination="pagination"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'user'">
            <div class="strong-line">{{ record.username }}</div>
            <div class="muted-line">{{ record.nickname || '-' }}</div>
          </template>
          <template v-else-if="column.key === 'contact'">
            <div>{{ record.email || '-' }}</div>
            <div class="muted-line">{{ record.phone || '-' }}</div>
          </template>
          <template v-else-if="column.key === 'roles'">
            <a-space wrap>
              <a-tag v-for="role in record.roles" :key="role">{{ role }}</a-tag>
              <span v-if="!record.roles?.length" class="muted-line">-</span>
            </a-space>
          </template>
          <template v-else-if="column.key === 'enabled'">
            <a-switch
              :checked="record.enabled"
              checked-children="启用"
              un-checked-children="禁用"
              @change="changeStatus(record.id, $event as boolean)"
            />
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button size="small" @click="openEdit(record.id)">编辑</a-button>
              <a-button size="small" @click="openRoles(record.id)">角色</a-button>
              <a-button size="small" @click="openResetPassword(record.id)">重置密码</a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-modal
      v-model:open="formOpen"
      :title="editingId ? '编辑用户' : '新增用户'"
      :confirm-loading="saving"
      width="720px"
      @ok="saveUser"
    >
      <a-form layout="vertical">
        <a-form-item label="用户名" required>
          <a-input v-model:value="form.username" />
        </a-form-item>
        <a-form-item v-if="!editingId" label="初始密码" required>
          <a-input-password v-model:value="form.password" />
        </a-form-item>
        <a-row :gutter="16">
          <a-col :span="8">
            <a-form-item label="昵称">
              <a-input v-model:value="form.nickname" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="邮箱">
              <a-input v-model:value="form.email" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="手机号">
              <a-input v-model:value="form.phone" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="状态">
          <a-switch v-model:checked="form.enabled" checked-children="启用" un-checked-children="禁用" />
        </a-form-item>
        <a-form-item v-if="!editingId" label="角色">
          <a-select
            v-model:value="form.roleIds"
            mode="multiple"
            show-search
            option-filter-prop="label"
            :options="roleOptions"
            :loading="rolesLoading"
            placeholder="选择角色"
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal
      v-model:open="passwordOpen"
      title="重置密码"
      :confirm-loading="passwordSaving"
      @ok="resetPassword"
    >
      <a-form layout="vertical">
        <a-form-item label="新密码" required>
          <a-input-password v-model:value="newPassword" />
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal
      v-model:open="rolesOpen"
      title="角色绑定"
      :confirm-loading="rolesSaving"
      @ok="saveRoles"
    >
      <a-form layout="vertical">
        <a-form-item label="角色">
          <a-select
            v-model:value="editingRoleIds"
            mode="multiple"
            show-search
            option-filter-prop="label"
            :options="roleOptions"
            :loading="rolesLoading"
            placeholder="选择角色"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { message, type TablePaginationConfig } from 'ant-design-vue';
import { PlusOutlined, ReloadOutlined, SearchOutlined } from '@ant-design/icons-vue';
import {
  createUser,
  fetchUser,
  fetchUsers,
  resetUserPassword,
  updateUser,
  updateUserRoles,
  updateUserStatus,
  type UserSummary,
} from '@/api/user';
import { fetchRoles, type RoleSummary } from '@/api/role';

const loading = ref(false);
const saving = ref(false);
const passwordSaving = ref(false);
const rolesSaving = ref(false);
const rolesLoading = ref(false);
const formOpen = ref(false);
const passwordOpen = ref(false);
const rolesOpen = ref(false);
const editingId = ref<number | null>(null);
const passwordUserId = ref<number | null>(null);
const rolesUserId = ref<number | null>(null);
const users = ref<UserSummary[]>([]);
const availableRoles = ref<RoleSummary[]>([]);
const total = ref(0);
const newPassword = ref('');
const editingRoleIds = ref<number[]>([]);

const query = reactive({
  keyword: '',
  status: undefined as number | undefined,
  pageNum: 1,
  pageSize: 10,
});

const form = reactive({
  username: '',
  password: '',
  nickname: '',
  email: '',
  phone: '',
  enabled: true,
  roleIds: [] as number[],
});

const statusOptions = [
  { label: '启用', value: 1 },
  { label: '禁用', value: 0 },
];

const columns = [
  { title: '用户', key: 'user', width: 180 },
  { title: '联系方式', key: 'contact', width: 240 },
  { title: '角色', key: 'roles', dataIndex: 'roles' },
  { title: '状态', key: 'enabled', width: 120 },
  { title: '最后登录', key: 'lastLoginAt', dataIndex: 'lastLoginAt', width: 190 },
  { title: '操作', key: 'action', width: 240 },
];

const pagination = computed<TablePaginationConfig>(() => ({
  current: query.pageNum,
  pageSize: query.pageSize,
  total: total.value,
  showSizeChanger: true,
  showTotal: (count) => `共 ${count} 条`,
}));

const roleOptions = computed(() =>
  availableRoles.value.map((role) => ({
    label: `${role.roleName}（${role.roleCode}）`,
    value: role.id,
  })),
);

onMounted(async () => {
  await Promise.all([loadUsers(), loadRoles()]);
});

async function loadRoles() {
  rolesLoading.value = true;
  try {
    const page = await fetchRoles({ pageNum: 1, pageSize: 100, status: 1 });
    availableRoles.value = page.records;
  } catch (error) {
    message.error(error instanceof Error ? error.message : '加载角色失败');
  } finally {
    rolesLoading.value = false;
  }
}

async function loadUsers() {
  loading.value = true;
  try {
    const page = await fetchUsers(query);
    users.value = page.records;
    total.value = page.total;
    query.pageNum = page.pageNum;
    query.pageSize = page.pageSize;
  } catch (error) {
    message.error(error instanceof Error ? error.message : '加载用户失败');
  } finally {
    loading.value = false;
  }
}

function handleSearch() {
  query.pageNum = 1;
  loadUsers();
}

function resetSearch() {
  query.keyword = '';
  query.status = undefined;
  query.pageNum = 1;
  loadUsers();
}

function handleTableChange(page: TablePaginationConfig) {
  query.pageNum = page.current || 1;
  query.pageSize = page.pageSize || 10;
  loadUsers();
}

function openCreate() {
  editingId.value = null;
  Object.assign(form, {
    username: '',
    password: '',
    nickname: '',
    email: '',
    phone: '',
    enabled: true,
    roleIds: [],
  });
  formOpen.value = true;
}

async function openEdit(id: number) {
  try {
    const user = await fetchUser(id);
    editingId.value = id;
    Object.assign(form, {
      username: user.username,
      password: '',
      nickname: user.nickname || '',
      email: user.email || '',
      phone: user.phone || '',
      enabled: user.enabled,
      roleIds: user.roleIds || [],
    });
    formOpen.value = true;
  } catch (error) {
    message.error(error instanceof Error ? error.message : '加载用户失败');
  }
}

async function saveUser() {
  if (!form.username.trim()) {
    message.warning('请填写用户名');
    return;
  }
  if (!editingId.value && !form.password.trim()) {
    message.warning('请填写初始密码');
    return;
  }
  saving.value = true;
  try {
    if (editingId.value) {
      await updateUser(editingId.value, {
        username: form.username.trim(),
        nickname: form.nickname,
        email: form.email,
        phone: form.phone,
        status: form.enabled ? 1 : 0,
      });
      message.success('用户已保存');
    } else {
      await createUser({
        username: form.username.trim(),
        password: form.password,
        nickname: form.nickname,
        email: form.email,
        phone: form.phone,
        status: form.enabled ? 1 : 0,
        roleIds: normalizeRoleIds(form.roleIds),
      });
      message.success('用户已创建');
    }
    formOpen.value = false;
    await loadUsers();
  } catch (error) {
    message.error(error instanceof Error ? error.message : '保存用户失败');
  } finally {
    saving.value = false;
  }
}

async function changeStatus(id: number, enabled: boolean) {
  try {
    await updateUserStatus(id, enabled ? 1 : 0);
    message.success('状态已更新');
    await loadUsers();
  } catch (error) {
    message.error(error instanceof Error ? error.message : '状态更新失败');
  }
}

function openResetPassword(id: number) {
  passwordUserId.value = id;
  newPassword.value = '';
  passwordOpen.value = true;
}

async function resetPassword() {
  if (!passwordUserId.value || !newPassword.value.trim()) {
    message.warning('请填写新密码');
    return;
  }
  passwordSaving.value = true;
  try {
    await resetUserPassword(passwordUserId.value, newPassword.value);
    message.success('密码已重置');
    passwordOpen.value = false;
  } catch (error) {
    message.error(error instanceof Error ? error.message : '重置密码失败');
  } finally {
    passwordSaving.value = false;
  }
}

async function openRoles(id: number) {
  try {
    const user = await fetchUser(id);
    rolesUserId.value = id;
    editingRoleIds.value = user.roleIds || [];
    rolesOpen.value = true;
  } catch (error) {
    message.error(error instanceof Error ? error.message : '加载角色失败');
  }
}

async function saveRoles() {
  if (!rolesUserId.value) {
    return;
  }
  rolesSaving.value = true;
  try {
    await updateUserRoles(rolesUserId.value, normalizeRoleIds(editingRoleIds.value));
    message.success('角色已更新');
    rolesOpen.value = false;
    await loadUsers();
  } catch (error) {
    message.error(error instanceof Error ? error.message : '角色更新失败');
  } finally {
    rolesSaving.value = false;
  }
}

function normalizeRoleIds(values: Array<string | number>) {
  return values
    .map((value) => Number(value))
    .filter((value) => Number.isInteger(value) && value > 0);
}
</script>
