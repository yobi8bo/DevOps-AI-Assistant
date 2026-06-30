<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h1>故障分类</h1>
        <p>维护智能排障、历史、案例、知识库和 Prompt 模板共用的故障类型。</p>
      </div>
      <a-space>
        <a-button @click="loadCategories">
          <reload-outlined />
          刷新
        </a-button>
        <a-button type="primary" @click="openCreate">
          <plus-outlined />
          新增分类
        </a-button>
      </a-space>
    </div>

    <a-card :bordered="false" class="filter-card">
      <a-form layout="inline">
        <a-form-item label="关键词">
          <a-input v-model:value="query.keyword" allow-clear placeholder="分类编码、名称或说明" @pressEnter="handleSearch" />
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
        :data-source="categories"
        :loading="loading"
        :pagination="pagination"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'category'">
            <div class="strong-line">{{ record.categoryName }}</div>
            <div class="muted-line">{{ record.categoryCode }}</div>
          </template>
          <template v-else-if="column.key === 'enabled'">
            <a-switch
              :checked="record.enabled"
              checked-children="启用"
              un-checked-children="停用"
              @change="changeStatus(record.id, $event as boolean)"
            />
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button size="small" @click="openEdit(record.id)">编辑</a-button>
              <a-popconfirm title="确认删除这个故障分类？" @confirm="removeCategory(record.id)">
                <a-button size="small" danger>删除</a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-modal
      v-model:open="formOpen"
      :title="editingId ? '编辑故障分类' : '新增故障分类'"
      :confirm-loading="saving"
      width="720px"
      @ok="saveCategory"
    >
      <a-form layout="vertical">
        <a-row :gutter="16">
          <a-col :span="10">
            <a-form-item label="分类编码" required>
              <a-input v-model:value="form.categoryCode" placeholder="例如 DOCKER" />
            </a-form-item>
          </a-col>
          <a-col :span="10">
            <a-form-item label="分类名称" required>
              <a-input v-model:value="form.categoryName" placeholder="例如 Docker" />
            </a-form-item>
          </a-col>
          <a-col :span="4">
            <a-form-item label="排序">
              <a-input-number v-model:value="form.sortOrder" :min="0" class="full-input" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="说明">
          <a-textarea v-model:value="form.description" :rows="3" />
        </a-form-item>
        <a-form-item label="状态">
          <a-switch v-model:checked="form.enabled" checked-children="启用" un-checked-children="停用" />
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
  createFaultCategory,
  deleteFaultCategory,
  fetchFaultCategory,
  fetchFaultCategories,
  updateFaultCategory,
  updateFaultCategoryStatus,
  type FaultCategorySummary,
} from '@/api/fault-category';

const loading = ref(false);
const saving = ref(false);
const formOpen = ref(false);
const editingId = ref<number | null>(null);
const categories = ref<FaultCategorySummary[]>([]);
const total = ref(0);

const query = reactive({
  keyword: '',
  status: undefined as number | undefined,
  pageNum: 1,
  pageSize: 10,
});

const form = reactive({
  categoryCode: '',
  categoryName: '',
  description: '',
  sortOrder: 0,
  enabled: true,
});

const statusOptions = [
  { label: '启用', value: 1 },
  { label: '停用', value: 0 },
];

const columns = [
  { title: '分类', key: 'category', width: 220 },
  { title: '说明', key: 'description', dataIndex: 'description', ellipsis: true },
  { title: '排序', key: 'sortOrder', dataIndex: 'sortOrder', width: 100 },
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

onMounted(loadCategories);

async function loadCategories() {
  loading.value = true;
  try {
    const page = await fetchFaultCategories(query);
    categories.value = page.records;
    total.value = page.total;
    query.pageNum = page.pageNum;
    query.pageSize = page.pageSize;
  } catch (error) {
    message.error(error instanceof Error ? error.message : '加载故障分类失败');
  } finally {
    loading.value = false;
  }
}

function handleSearch() {
  query.pageNum = 1;
  loadCategories();
}

function resetSearch() {
  query.keyword = '';
  query.status = undefined;
  query.pageNum = 1;
  loadCategories();
}

function handleTableChange(page: TablePaginationConfig) {
  query.pageNum = page.current || 1;
  query.pageSize = page.pageSize || 10;
  loadCategories();
}

function openCreate() {
  editingId.value = null;
  Object.assign(form, {
    categoryCode: '',
    categoryName: '',
    description: '',
    sortOrder: 0,
    enabled: true,
  });
  formOpen.value = true;
}

async function openEdit(id: number) {
  try {
    const item = await fetchFaultCategory(id);
    editingId.value = id;
    Object.assign(form, {
      categoryCode: item.categoryCode,
      categoryName: item.categoryName,
      description: item.description || '',
      sortOrder: item.sortOrder || 0,
      enabled: item.enabled,
    });
    formOpen.value = true;
  } catch (error) {
    message.error(error instanceof Error ? error.message : '加载分类失败');
  }
}

async function saveCategory() {
  if (!form.categoryCode.trim()) {
    message.warning('请填写分类编码');
    return;
  }
  if (!form.categoryName.trim()) {
    message.warning('请填写分类名称');
    return;
  }
  saving.value = true;
  try {
    const payload = {
      categoryCode: form.categoryCode.trim(),
      categoryName: form.categoryName.trim(),
      description: form.description,
      sortOrder: form.sortOrder || 0,
      status: form.enabled ? 1 : 0,
    };
    if (editingId.value) {
      await updateFaultCategory(editingId.value, payload);
      message.success('分类已保存');
    } else {
      await createFaultCategory(payload);
      message.success('分类已创建');
    }
    formOpen.value = false;
    await loadCategories();
  } catch (error) {
    message.error(error instanceof Error ? error.message : '保存分类失败');
  } finally {
    saving.value = false;
  }
}

async function changeStatus(id: number, enabled: boolean) {
  try {
    await updateFaultCategoryStatus(id, enabled ? 1 : 0);
    message.success('状态已更新');
    await loadCategories();
  } catch (error) {
    message.error(error instanceof Error ? error.message : '状态更新失败');
  }
}

async function removeCategory(id: number) {
  try {
    await deleteFaultCategory(id);
    message.success('分类已删除');
    await loadCategories();
  } catch (error) {
    message.error(error instanceof Error ? error.message : '删除分类失败');
  }
}
</script>

<style scoped>
.full-input {
  width: 100%;
}
</style>
