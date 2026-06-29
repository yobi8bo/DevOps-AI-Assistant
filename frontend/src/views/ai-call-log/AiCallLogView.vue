<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h1>AI 调用日志</h1>
        <p>查看模型调用状态、耗时、Token 用量和失败原因。</p>
      </div>
      <a-button @click="loadLogs">
        <reload-outlined />
        刷新
      </a-button>
    </div>

    <a-card :bordered="false" class="filter-card">
      <a-form layout="inline">
        <a-form-item label="关键词">
          <a-input v-model:value="query.keyword" allow-clear placeholder="请求ID、模型或错误信息" @pressEnter="handleSearch" />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="successFilter" allow-clear :options="successOptions" class="filter-select" />
        </a-form-item>
        <a-form-item label="模型配置ID">
          <a-input-number v-model:value="query.modelConfigId" :min="1" class="number-filter" />
        </a-form-item>
        <a-form-item label="会话ID">
          <a-input-number v-model:value="query.sessionId" :min="1" class="number-filter" />
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
          <template v-if="column.key === 'requestId'">
            <div class="mono-text">{{ record.requestId }}</div>
            <div class="muted-line">会话 {{ record.sessionId || '-' }} · 用户 {{ record.userId || '-' }}</div>
          </template>
          <template v-else-if="column.key === 'model'">
            <div class="strong-line">{{ record.modelName || '-' }}</div>
            <div class="muted-line">{{ record.provider || '-' }} · 配置 {{ record.modelConfigId || '-' }}</div>
          </template>
          <template v-else-if="column.key === 'success'">
            <a-tag :color="record.success ? 'green' : 'red'">
              {{ record.success ? '成功' : '失败' }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'tokens'">
            <span>{{ record.totalTokens || 0 }}</span>
            <div class="muted-line">in {{ record.promptTokens || 0 }} / out {{ record.completionTokens || 0 }}</div>
          </template>
          <template v-else-if="column.key === 'latencyMs'">
            <span>{{ record.latencyMs || 0 }} ms</span>
          </template>
          <template v-else-if="column.key === 'error'">
            <a-button v-if="record.errorMessage" type="link" class="table-link" @click="openError(record)">
              {{ record.errorCode || '错误详情' }}
            </a-button>
            <span v-else>-</span>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-modal v-model:open="errorOpen" title="错误详情" :footer="null" width="720px">
      <a-descriptions v-if="selectedLog" bordered size="small" :column="1">
        <a-descriptions-item label="请求ID">{{ selectedLog.requestId }}</a-descriptions-item>
        <a-descriptions-item label="错误码">{{ selectedLog.errorCode || '-' }}</a-descriptions-item>
        <a-descriptions-item label="错误信息">
          <pre class="message-content">{{ selectedLog.errorMessage }}</pre>
        </a-descriptions-item>
      </a-descriptions>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { message, type TablePaginationConfig } from 'ant-design-vue';
import { ReloadOutlined, SearchOutlined } from '@ant-design/icons-vue';
import { fetchAiCallLogs, type AiCallLogItem } from '@/api/ai-call-log';

const loading = ref(false);
const logs = ref<AiCallLogItem[]>([]);
const total = ref(0);
const successFilter = ref<string | undefined>();
const errorOpen = ref(false);
const selectedLog = ref<AiCallLogItem | null>(null);

const query = reactive({
  keyword: '',
  modelConfigId: undefined as number | undefined,
  sessionId: undefined as number | undefined,
  pageNum: 1,
  pageSize: 10,
});

const successOptions = [
  { label: '成功', value: 'true' },
  { label: '失败', value: 'false' },
];

const columns = [
  { title: '请求', key: 'requestId', width: 300 },
  { title: '模型', key: 'model', width: 220 },
  { title: '状态', key: 'success', width: 100 },
  { title: 'Token', key: 'tokens', width: 130 },
  { title: '耗时', key: 'latencyMs', dataIndex: 'latencyMs', width: 110 },
  { title: '错误', key: 'error', width: 150 },
  { title: '调用时间', key: 'createdAt', dataIndex: 'createdAt', width: 190 },
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
    const page = await fetchAiCallLogs({
      ...query,
      success: successFilter.value ? successFilter.value === 'true' : undefined,
    });
    logs.value = page.records;
    total.value = page.total;
    query.pageNum = page.pageNum;
    query.pageSize = page.pageSize;
  } catch (error) {
    message.error(error instanceof Error ? error.message : '加载 AI 调用日志失败');
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
  query.modelConfigId = undefined;
  query.sessionId = undefined;
  successFilter.value = undefined;
  query.pageNum = 1;
  loadLogs();
}

function handleTableChange(page: TablePaginationConfig) {
  query.pageNum = page.current || 1;
  query.pageSize = page.pageSize || 10;
  loadLogs();
}

function openError(record: AiCallLogItem) {
  selectedLog.value = record;
  errorOpen.value = true;
}
</script>
