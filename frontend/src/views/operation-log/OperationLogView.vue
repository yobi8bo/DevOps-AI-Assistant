<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h1>系统日志</h1>
        <p>查看后台关键操作记录、请求来源和失败原因。</p>
      </div>
      <a-button @click="loadLogs">
        <reload-outlined />
        刷新
      </a-button>
    </div>

    <a-card :bordered="false" class="filter-card">
      <a-form layout="inline">
        <a-form-item label="关键词">
          <a-input v-model:value="query.keyword" allow-clear placeholder="用户名、URI 或错误信息" @pressEnter="handleSearch" />
        </a-form-item>
        <a-form-item label="模块">
          <a-select v-model:value="query.module" allow-clear :options="moduleOptions" class="filter-select" />
        </a-form-item>
        <a-form-item label="动作">
          <a-select v-model:value="query.action" allow-clear :options="actionOptions" class="filter-select" />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="successFilter" allow-clear :options="successOptions" class="filter-select" />
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
        :data-source="logs"
        :loading="loading"
        :pagination="pagination"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'operator'">
            <div class="strong-line">{{ record.username || '-' }}</div>
            <div class="muted-line">用户 {{ record.userId || '-' }}</div>
          </template>
          <template v-else-if="column.key === 'operation'">
            <a-tag>{{ record.module || '-' }}</a-tag>
            <a-tag :color="actionColor(record.action)">{{ record.action || '-' }}</a-tag>
            <div class="muted-line">目标 {{ record.targetId || '-' }}</div>
          </template>
          <template v-else-if="column.key === 'request'">
            <div class="mono-text">{{ record.requestMethod || '-' }} {{ record.requestUri || '-' }}</div>
            <div class="muted-line">{{ record.ipAddress || '-' }}</div>
          </template>
          <template v-else-if="column.key === 'success'">
            <a-tag :color="record.success ? 'green' : 'red'">
              {{ record.success ? '成功' : '失败' }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'error'">
            <a-button v-if="record.errorMessage" type="link" class="table-link" @click="openError(record)">
              错误详情
            </a-button>
            <span v-else>-</span>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-modal v-model:open="detailOpen" title="日志详情" :footer="null" width="780px">
      <a-descriptions v-if="selectedLog" bordered size="small" :column="1">
        <a-descriptions-item label="操作人">{{ selectedLog.username || '-' }} / {{ selectedLog.userId || '-' }}</a-descriptions-item>
        <a-descriptions-item label="模块动作">{{ selectedLog.module || '-' }} / {{ selectedLog.action || '-' }}</a-descriptions-item>
        <a-descriptions-item label="请求">{{ selectedLog.requestMethod || '-' }} {{ selectedLog.requestUri || '-' }}</a-descriptions-item>
        <a-descriptions-item label="来源 IP">{{ selectedLog.ipAddress || '-' }}</a-descriptions-item>
        <a-descriptions-item label="User Agent">{{ selectedLog.userAgent || '-' }}</a-descriptions-item>
        <a-descriptions-item label="状态">{{ selectedLog.success ? '成功' : '失败' }}</a-descriptions-item>
        <a-descriptions-item label="错误信息">
          <pre class="message-content">{{ selectedLog.errorMessage || '-' }}</pre>
        </a-descriptions-item>
      </a-descriptions>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { message, type TablePaginationConfig } from 'ant-design-vue';
import { ReloadOutlined, SearchOutlined } from '@ant-design/icons-vue';
import { fetchOperationLogs, type OperationLogItem } from '@/api/operation-log';

const loading = ref(false);
const logs = ref<OperationLogItem[]>([]);
const total = ref(0);
const successFilter = ref<string | undefined>();
const detailOpen = ref(false);
const selectedLog = ref<OperationLogItem | null>(null);

const query = reactive({
  keyword: '',
  module: undefined as string | undefined,
  action: undefined as string | undefined,
  pageNum: 1,
  pageSize: 10,
});

const moduleOptions = [
  '用户管理',
  '角色权限',
  '故障分类',
  '案例库',
  '知识库',
  '复盘报告',
  '模型配置',
  'Prompt模板',
  '智能排障',
].map((value) => ({ label: value, value }));

const actionOptions = [
  { label: '新增', value: '新增' },
  { label: '修改', value: '修改' },
  { label: '状态变更', value: '状态变更' },
  { label: '删除', value: '删除' },
];

const successOptions = [
  { label: '成功', value: 'true' },
  { label: '失败', value: 'false' },
];

const columns = [
  { title: '操作人', key: 'operator', width: 150 },
  { title: '操作', key: 'operation', width: 220 },
  { title: '请求', key: 'request' },
  { title: '状态', key: 'success', width: 100 },
  { title: '错误', key: 'error', width: 120 },
  { title: '时间', key: 'createdAt', dataIndex: 'createdAt', width: 190 },
];

const pagination = computed<TablePaginationConfig>(() => ({
  current: query.pageNum,
  pageSize: query.pageSize,
  total: total.value,
  showSizeChanger: true,
  showTotal: (count) => `共 ${count} 条`,
}));

onMounted(loadLogs);

async function loadLogs() {
  loading.value = true;
  try {
    const page = await fetchOperationLogs({
      ...query,
      success: successFilter.value ? successFilter.value === 'true' : undefined,
    });
    logs.value = page.records;
    total.value = page.total;
    query.pageNum = page.pageNum;
    query.pageSize = page.pageSize;
  } catch (error) {
    message.error(error instanceof Error ? error.message : '加载系统日志失败');
  } finally {
    loading.value = false;
  }
}

function handleSearch() {
  query.pageNum = 1;
  loadLogs();
}

function resetSearch() {
  query.keyword = '';
  query.module = undefined;
  query.action = undefined;
  successFilter.value = undefined;
  query.pageNum = 1;
  loadLogs();
}

function handleTableChange(page: TablePaginationConfig) {
  query.pageNum = page.current || 1;
  query.pageSize = page.pageSize || 10;
  loadLogs();
}

function openError(record: OperationLogItem) {
  selectedLog.value = record;
  detailOpen.value = true;
}

function actionColor(action?: string) {
  const colors: Record<string, string> = {
    新增: 'green',
    修改: 'blue',
    状态变更: 'orange',
    删除: 'red',
  };
  return action ? colors[action] || 'default' : 'default';
}
</script>
