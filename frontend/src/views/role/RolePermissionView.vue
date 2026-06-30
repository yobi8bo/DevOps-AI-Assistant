<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h1>角色权限</h1>
        <p>维护后台角色、启停状态和权限绑定。</p>
      </div>
      <a-space>
        <a-button @click="reloadAll">
          <reload-outlined />
          刷新
        </a-button>
        <a-button type="primary" @click="openCreate">
          <plus-outlined />
          新增角色
        </a-button>
      </a-space>
    </div>

    <a-card :bordered="false" class="filter-card">
      <a-form layout="inline">
        <a-form-item label="关键词">
          <a-input v-model:value="query.keyword" allow-clear placeholder="角色编码、名称或说明" @pressEnter="handleSearch" />
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
        :data-source="roles"
        :loading="loading"
        :pagination="pagination"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'role'">
            <div class="strong-line">{{ record.roleName }}</div>
            <div class="muted-line">{{ record.roleCode }}</div>
          </template>
          <template v-else-if="column.key === 'permissions'">
            <a-space wrap>
              <a-tag v-for="permission in record.permissions" :key="permission">{{ permission }}</a-tag>
              <span v-if="!record.permissions?.length" class="muted-line">-</span>
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
              <a-button size="small" @click="openPermissions(record.id)">权限</a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-modal
      v-model:open="formOpen"
      :title="editingId ? '编辑角色' : '新增角色'"
      :confirm-loading="saving"
      width="720px"
      @ok="saveRole"
    >
      <a-form layout="vertical">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="角色编码" required>
              <a-input v-model:value="form.roleCode" placeholder="例如 ADMIN" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="角色名称" required>
              <a-input v-model:value="form.roleName" placeholder="例如 管理员" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="说明">
          <a-textarea v-model:value="form.description" :rows="3" />
        </a-form-item>
        <a-form-item label="状态">
          <a-switch v-model:checked="form.enabled" checked-children="启用" un-checked-children="禁用" />
        </a-form-item>
        <a-form-item v-if="!editingId" label="权限">
          <a-select
            v-model:value="form.permissionIds"
            mode="multiple"
            show-search
            option-filter-prop="label"
            :options="permissionOptions"
            :loading="permissionsLoading"
            placeholder="选择权限"
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal
      v-model:open="permissionsOpen"
      title="权限绑定"
      :confirm-loading="permissionsSaving"
      width="720px"
      @ok="savePermissions"
    >
      <a-form layout="vertical">
        <a-form-item label="权限">
          <a-select
            v-model:value="editingPermissionIds"
            mode="multiple"
            show-search
            option-filter-prop="label"
            :options="permissionOptions"
            :loading="permissionsLoading"
            placeholder="选择权限"
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
  createRole,
  fetchPermissions,
  fetchRole,
  fetchRoles,
  updateRole,
  updateRolePermissions,
  updateRoleStatus,
  type PermissionSummary,
  type RoleSummary,
} from '@/api/role';

const loading = ref(false);
const saving = ref(false);
const permissionsLoading = ref(false);
const permissionsSaving = ref(false);
const formOpen = ref(false);
const permissionsOpen = ref(false);
const editingId = ref<number | null>(null);
const permissionsRoleId = ref<number | null>(null);
const roles = ref<RoleSummary[]>([]);
const permissions = ref<PermissionSummary[]>([]);
const total = ref(0);
const editingPermissionIds = ref<number[]>([]);

const query = reactive({
  keyword: '',
  status: undefined as number | undefined,
  pageNum: 1,
  pageSize: 10,
});

const form = reactive({
  roleCode: '',
  roleName: '',
  description: '',
  enabled: true,
  permissionIds: [] as number[],
});

const statusOptions = [
  { label: '启用', value: 1 },
  { label: '禁用', value: 0 },
];

const columns = [
  { title: '角色', key: 'role', width: 220 },
  { title: '说明', key: 'description', dataIndex: 'description', ellipsis: true },
  { title: '权限', key: 'permissions', width: 360 },
  { title: '状态', key: 'enabled', width: 120 },
  { title: '更新时间', key: 'updatedAt', dataIndex: 'updatedAt', width: 190 },
  { title: '操作', key: 'action', width: 150 },
];

const pagination = computed<TablePaginationConfig>(() => ({
  current: query.pageNum,
  pageSize: query.pageSize,
  total: total.value,
  showSizeChanger: true,
  showTotal: (count) => `共 ${count} 条`,
}));

const permissionOptions = computed(() =>
  permissions.value.map((permission) => ({
    label: `${permission.permissionName}（${permission.permissionCode}）`,
    value: permission.id,
  })),
);

onMounted(async () => {
  await Promise.all([loadRoles(), loadPermissions()]);
});

async function reloadAll() {
  await Promise.all([loadRoles(), loadPermissions()]);
}

async function loadRoles() {
  loading.value = true;
  try {
    const page = await fetchRoles(query);
    roles.value = page.records;
    total.value = page.total;
    query.pageNum = page.pageNum;
    query.pageSize = page.pageSize;
  } catch (error) {
    message.error(error instanceof Error ? error.message : '加载角色失败');
  } finally {
    loading.value = false;
  }
}

async function loadPermissions() {
  permissionsLoading.value = true;
  try {
    permissions.value = await fetchPermissions();
  } catch (error) {
    message.error(error instanceof Error ? error.message : '加载权限失败');
  } finally {
    permissionsLoading.value = false;
  }
}

function handleSearch() {
  query.pageNum = 1;
  loadRoles();
}

function resetSearch() {
  query.keyword = '';
  query.status = undefined;
  query.pageNum = 1;
  loadRoles();
}

function handleTableChange(page: TablePaginationConfig) {
  query.pageNum = page.current || 1;
  query.pageSize = page.pageSize || 10;
  loadRoles();
}

function openCreate() {
  editingId.value = null;
  Object.assign(form, {
    roleCode: '',
    roleName: '',
    description: '',
    enabled: true,
    permissionIds: [],
  });
  formOpen.value = true;
}

async function openEdit(id: number) {
  try {
    const role = await fetchRole(id);
    editingId.value = id;
    Object.assign(form, {
      roleCode: role.roleCode,
      roleName: role.roleName,
      description: role.description || '',
      enabled: role.enabled,
      permissionIds: role.permissionIds || [],
    });
    formOpen.value = true;
  } catch (error) {
    message.error(error instanceof Error ? error.message : '加载角色失败');
  }
}

async function saveRole() {
  if (!form.roleCode.trim()) {
    message.warning('请填写角色编码');
    return;
  }
  if (!form.roleName.trim()) {
    message.warning('请填写角色名称');
    return;
  }
  saving.value = true;
  try {
    if (editingId.value) {
      await updateRole(editingId.value, {
        roleCode: form.roleCode.trim(),
        roleName: form.roleName.trim(),
        description: form.description,
        status: form.enabled ? 1 : 0,
      });
      message.success('角色已保存');
    } else {
      await createRole({
        roleCode: form.roleCode.trim(),
        roleName: form.roleName.trim(),
        description: form.description,
        status: form.enabled ? 1 : 0,
        permissionIds: form.permissionIds,
      });
      message.success('角色已创建');
    }
    formOpen.value = false;
    await loadRoles();
  } catch (error) {
    message.error(error instanceof Error ? error.message : '保存角色失败');
  } finally {
    saving.value = false;
  }
}

async function changeStatus(id: number, enabled: boolean) {
  try {
    await updateRoleStatus(id, enabled ? 1 : 0);
    message.success('状态已更新');
    await loadRoles();
  } catch (error) {
    message.error(error instanceof Error ? error.message : '状态更新失败');
  }
}

async function openPermissions(id: number) {
  try {
    const role = await fetchRole(id);
    permissionsRoleId.value = id;
    editingPermissionIds.value = role.permissionIds || [];
    permissionsOpen.value = true;
  } catch (error) {
    message.error(error instanceof Error ? error.message : '加载权限失败');
  }
}

async function savePermissions() {
  if (!permissionsRoleId.value) {
    return;
  }
  permissionsSaving.value = true;
  try {
    await updateRolePermissions(permissionsRoleId.value, editingPermissionIds.value);
    message.success('权限已更新');
    permissionsOpen.value = false;
    await loadRoles();
  } catch (error) {
    message.error(error instanceof Error ? error.message : '权限更新失败');
  } finally {
    permissionsSaving.value = false;
  }
}
</script>
