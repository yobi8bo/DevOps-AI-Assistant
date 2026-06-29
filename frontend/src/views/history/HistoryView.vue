<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h1>排障历史</h1>
        <p>查看已保存的排障会话、分析结果和处理状态。</p>
      </div>
      <a-button @click="loadSessions">
        <reload-outlined />
        刷新
      </a-button>
    </div>

    <a-card :bordered="false" class="filter-card">
      <a-form layout="inline">
        <a-form-item label="关键词">
          <a-input v-model:value="query.keyword" allow-clear placeholder="标题关键词" @pressEnter="handleSearch" />
        </a-form-item>
        <a-form-item label="类型">
          <a-select v-model:value="query.category" allow-clear :options="categoryOptions" class="filter-select" />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="query.status" allow-clear :options="statusOptions" class="filter-select" />
        </a-form-item>
        <a-form-item label="生产">
          <a-select v-model:value="productionFilter" allow-clear :options="productionOptions" class="filter-select" />
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
        :data-source="sessions"
        :loading="loading"
        :pagination="pagination"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'title'">
            <a-button type="link" class="table-link" @click="openDetail(record.id)">
              {{ record.title }}
            </a-button>
            <div class="muted-line">{{ record.summary || record.environment || '-' }}</div>
          </template>
          <template v-else-if="column.key === 'isProduction'">
            <a-tag :color="record.isProduction ? 'red' : 'green'">
              {{ record.isProduction ? '生产' : '非生产' }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'status'">
            <a-select
              :value="record.status"
              size="small"
              :options="statusOptions"
              class="status-select"
              @change="changeStatus(record.id, $event as string)"
            />
          </template>
          <template v-else-if="column.key === 'riskLevel'">
            <a-tag :color="riskColor(record.riskLevel)">
              {{ record.riskLevel }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button size="small" @click="openDetail(record.id)">查看</a-button>
              <a-popconfirm title="确认删除这条排障记录？" @confirm="deleteSession(record.id)">
                <a-button size="small" danger>删除</a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-drawer
      v-model:open="detailOpen"
      width="min(920px, 100vw)"
      :title="detail?.title || '排障详情'"
      :destroy-on-close="true"
    >
      <a-spin :spinning="detailLoading">
        <a-empty v-if="!detail" description="暂无详情" />
        <div v-else class="detail-content">
          <a-descriptions bordered size="small" :column="2">
            <a-descriptions-item label="故障类型">{{ detail.category || '-' }}</a-descriptions-item>
            <a-descriptions-item label="状态">{{ statusLabel(detail.status) }}</a-descriptions-item>
            <a-descriptions-item label="运行环境">{{ detail.environment || '-' }}</a-descriptions-item>
            <a-descriptions-item label="生产环境">{{ detail.isProduction ? '是' : '否' }}</a-descriptions-item>
            <a-descriptions-item label="紧急程度">{{ detail.urgencyLevel || '-' }}</a-descriptions-item>
            <a-descriptions-item label="风险等级">
              <a-tag :color="riskColor(detail.latestResult?.riskLevel || 'LOW')">
                {{ detail.latestResult?.riskLevel || 'LOW' }}
              </a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="模型配置ID">{{ detail.latestResult?.modelConfigId || '-' }}</a-descriptions-item>
            <a-descriptions-item label="Prompt版本">
              {{ detail.latestResult?.promptTemplateId || '-' }} / {{ detail.latestResult?.promptVersion || '-' }}
            </a-descriptions-item>
          </a-descriptions>

          <a-card title="最新结论" :bordered="false">
            <p>{{ detail.latestResult?.summary || '-' }}</p>
          </a-card>

          <a-card title="继续分析" :bordered="false">
            <a-space direction="vertical" class="follow-up-box">
              <a-textarea
                v-model:value="followUpContent"
                :rows="4"
                placeholder="补充新的日志、命令输出或排查结果"
              />
              <a-button type="primary" :loading="followUpLoading" @click="submitFollowUp">
                发送追问
              </a-button>
            </a-space>
          </a-card>

          <a-card title="会话消息" :bordered="false">
            <a-timeline>
              <a-timeline-item v-for="item in detail.messages" :key="item.id">
                <div class="message-title">
                  <a-tag :color="item.role === 'user' ? 'blue' : 'purple'">{{ item.role }}</a-tag>
                  <span>{{ item.createdAt }}</span>
                </div>
                <pre class="message-content">{{ item.content }}</pre>
              </a-timeline-item>
            </a-timeline>
          </a-card>
        </div>
      </a-spin>
    </a-drawer>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { message, type TablePaginationConfig } from 'ant-design-vue';
import { ReloadOutlined, SearchOutlined } from '@ant-design/icons-vue';
import {
  continueDiagnosisSession,
  deleteDiagnosisSession,
  fetchDiagnosisSession,
  fetchDiagnosisSessions,
  updateDiagnosisSessionStatus,
  type SessionDetail,
  type SessionSummary,
} from '@/api/diagnosis';

const loading = ref(false);
const detailLoading = ref(false);
const followUpLoading = ref(false);
const detailOpen = ref(false);
const detail = ref<SessionDetail | null>(null);
const sessions = ref<SessionSummary[]>([]);
const total = ref(0);
const productionFilter = ref<string | undefined>();
const followUpContent = ref('');

const query = reactive({
  keyword: '',
  category: undefined as string | undefined,
  status: undefined as string | undefined,
  pageNum: 1,
  pageSize: 10,
});

const categoryOptions = [
  'Linux',
  'Docker',
  'Kubernetes',
  'Jenkins',
  'Nginx',
  'MySQL',
  'Redis',
  'Spring Boot',
].map((value) => ({ label: value, value }));

const statusOptions = [
  { label: '分析中', value: 'ANALYZING' },
  { label: '待确认', value: 'WAITING_CONFIRM' },
  { label: '已解决', value: 'RESOLVED' },
  { label: '未解决', value: 'UNRESOLVED' },
  { label: '已归档', value: 'ARCHIVED' },
];

const productionOptions = [
  { label: '生产环境', value: 'true' },
  { label: '非生产环境', value: 'false' },
];

const columns = [
  { title: '故障标题', key: 'title', dataIndex: 'title' },
  { title: '类型', key: 'category', dataIndex: 'category', width: 120 },
  { title: '环境', key: 'isProduction', dataIndex: 'isProduction', width: 110 },
  { title: '状态', key: 'status', dataIndex: 'status', width: 150 },
  { title: '风险', key: 'riskLevel', dataIndex: 'riskLevel', width: 110 },
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

onMounted(loadSessions);

async function loadSessions() {
  loading.value = true;
  try {
    const page = await fetchDiagnosisSessions({
      ...query,
      isProduction: productionFilter.value ? productionFilter.value === 'true' : undefined,
    });
    sessions.value = page.records;
    total.value = page.total;
    query.pageNum = page.pageNum;
    query.pageSize = page.pageSize;
  } catch (error) {
    message.error(error instanceof Error ? error.message : '加载历史记录失败');
  } finally {
    loading.value = false;
  }
}

function handleSearch() {
  query.pageNum = 1;
  loadSessions();
}

function resetSearch() {
  query.keyword = '';
  query.category = undefined;
  query.status = undefined;
  productionFilter.value = undefined;
  query.pageNum = 1;
  loadSessions();
}

function handleTableChange(page: TablePaginationConfig) {
  query.pageNum = page.current || 1;
  query.pageSize = page.pageSize || 10;
  loadSessions();
}

async function openDetail(id: number) {
  detailOpen.value = true;
  detailLoading.value = true;
  detail.value = null;
  followUpContent.value = '';
  try {
    detail.value = await fetchDiagnosisSession(id);
  } catch (error) {
    message.error(error instanceof Error ? error.message : '加载详情失败');
  } finally {
    detailLoading.value = false;
  }
}

async function submitFollowUp() {
  if (!detail.value?.id) {
    message.warning('请先打开排障详情');
    return;
  }
  if (!followUpContent.value.trim()) {
    message.warning('请输入追问内容');
    return;
  }
  const sessionId = detail.value.id;
  followUpLoading.value = true;
  try {
    await continueDiagnosisSession(sessionId, {
      content: followUpContent.value.trim(),
    });
    followUpContent.value = '';
    detail.value = await fetchDiagnosisSession(sessionId);
    await loadSessions();
    message.success('分析已更新');
  } catch (error) {
    message.error(error instanceof Error ? error.message : '继续分析失败');
  } finally {
    followUpLoading.value = false;
  }
}

async function changeStatus(id: number, status: string) {
  try {
    await updateDiagnosisSessionStatus(id, status);
    message.success('状态已更新');
    await loadSessions();
    if (detail.value?.id === id) {
      detail.value = await fetchDiagnosisSession(id);
    }
  } catch (error) {
    message.error(error instanceof Error ? error.message : '状态更新失败');
  }
}

async function deleteSession(id: number) {
  try {
    await deleteDiagnosisSession(id);
    message.success('已删除');
    if (detail.value?.id === id) {
      detailOpen.value = false;
      detail.value = null;
    }
    await loadSessions();
  } catch (error) {
    message.error(error instanceof Error ? error.message : '删除失败');
  }
}

function riskColor(level: string) {
  const colors: Record<string, string> = {
    LOW: 'green',
    MEDIUM: 'orange',
    HIGH: 'red',
    CRITICAL: 'volcano',
  };
  return colors[level] || 'default';
}

function statusLabel(status: string) {
  return statusOptions.find((item) => item.value === status)?.label || status;
}
</script>
