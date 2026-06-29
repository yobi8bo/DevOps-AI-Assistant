<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h1>工作台</h1>
        <p>查看排障概览、近期分析和系统运行状态。</p>
      </div>
      <a-button @click="loadDashboard">
        <reload-outlined />
        刷新
      </a-button>
    </div>

    <a-spin :spinning="loading">
      <a-row :gutter="[16, 16]">
        <a-col :xs="24" :sm="12" :lg="6" v-for="item in stats" :key="item.label">
          <a-card :bordered="false" class="metric-card">
            <div class="metric-card__top">
              <component :is="item.icon" />
              <a-tag :color="item.color">{{ item.tag }}</a-tag>
            </div>
            <a-statistic :title="item.label" :value="item.value" />
            <div class="metric-card__hint">{{ item.hint }}</div>
          </a-card>
        </a-col>
      </a-row>

      <a-row :gutter="[16, 16]" class="section">
        <a-col :xs="24" :xl="14">
          <a-card title="最近分析" :bordered="false" class="dashboard-panel">
            <a-table
              v-if="recentSessions.length"
              row-key="id"
              size="small"
              :columns="recentColumns"
              :data-source="recentSessions"
              :pagination="false"
            >
              <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'title'">
                  <router-link class="table-link" to="/history">{{ record.title }}</router-link>
                  <div class="muted-line">{{ record.summary || record.environment || '-' }}</div>
                </template>
                <template v-else-if="column.key === 'riskLevel'">
                  <a-tag :color="riskColor(record.riskLevel)">{{ record.riskLevel }}</a-tag>
                </template>
                <template v-else-if="column.key === 'status'">
                  <a-tag>{{ statusLabel(record.status) }}</a-tag>
                </template>
              </template>
            </a-table>
            <a-empty v-else description="暂无排障记录" />
          </a-card>
        </a-col>

        <a-col :xs="24" :xl="10">
          <a-card title="运行状态" :bordered="false" class="dashboard-panel">
            <div class="health-list">
              <div class="health-item">
                <span>模型配置</span>
                <strong>{{ modelConfigTotal }}</strong>
              </div>
              <div class="health-item">
                <span>Prompt 模板</span>
                <strong>{{ promptTemplateTotal }}</strong>
              </div>
              <div class="health-item">
                <span>AI 调用成功率</span>
                <strong>{{ aiSuccessRate }}%</strong>
              </div>
              <div class="health-item">
                <span>平均耗时</span>
                <strong>{{ averageLatency }} ms</strong>
              </div>
            </div>
          </a-card>
        </a-col>
      </a-row>

    </a-spin>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { message } from 'ant-design-vue';
import {
  ApiOutlined,
  CheckCircleOutlined,
  ClockCircleOutlined,
  ExclamationCircleOutlined,
  ReloadOutlined,
} from '@ant-design/icons-vue';
import { fetchAiCallLogs, type AiCallLogItem } from '@/api/ai-call-log';
import { fetchDiagnosisSessions, type SessionSummary } from '@/api/diagnosis';
import { fetchModelConfigs } from '@/api/model-config';
import { fetchPromptTemplates } from '@/api/prompt-template';

const loading = ref(false);
const recentSessions = ref<SessionSummary[]>([]);
const recentLogs = ref<AiCallLogItem[]>([]);
const modelConfigTotal = ref(0);
const promptTemplateTotal = ref(0);
const aiLogTotal = ref(0);

const recentColumns = [
  { title: '故障', key: 'title', dataIndex: 'title' },
  { title: '风险', key: 'riskLevel', dataIndex: 'riskLevel', width: 110 },
  { title: '状态', key: 'status', dataIndex: 'status', width: 110 },
  { title: '更新时间', key: 'updatedAt', dataIndex: 'updatedAt', width: 180 },
];

const todayCount = computed(() => {
  const today = new Date().toISOString().slice(0, 10);
  return recentSessions.value.filter((item) => item.createdAt?.startsWith(today)).length;
});

const resolvedCount = computed(() => recentSessions.value.filter((item) => item.status === 'RESOLVED').length);
const waitingCount = computed(() => recentSessions.value.filter((item) => item.status === 'WAITING_CONFIRM').length);
const failedAiCount = computed(() => recentLogs.value.filter((item) => !item.success).length);
const aiSuccessRate = computed(() => {
  if (recentLogs.value.length === 0) {
    return 100;
  }
  const successCount = recentLogs.value.filter((item) => item.success).length;
  return Math.round((successCount / recentLogs.value.length) * 100);
});
const averageLatency = computed(() => {
  if (recentLogs.value.length === 0) {
    return 0;
  }
  const total = recentLogs.value.reduce((sum, item) => sum + (item.latencyMs || 0), 0);
  return Math.round(total / recentLogs.value.length);
});

const stats = computed(() => [
  {
    label: '今日排障',
    value: todayCount.value,
    tag: 'Today',
    color: 'blue',
    hint: '基于最近记录统计',
    icon: ClockCircleOutlined,
  },
  {
    label: '已解决',
    value: resolvedCount.value,
    tag: 'Done',
    color: 'green',
    hint: '最近排障记录',
    icon: CheckCircleOutlined,
  },
  {
    label: '待确认',
    value: waitingCount.value,
    tag: 'Pending',
    color: 'orange',
    hint: '需要人工确认',
    icon: ExclamationCircleOutlined,
  },
  {
    label: 'AI 调用',
    value: aiLogTotal.value,
    tag: failedAiCount.value ? `${failedAiCount.value} 失败` : 'OK',
    color: failedAiCount.value ? 'red' : 'purple',
    hint: '最近调用日志总数',
    icon: ApiOutlined,
  },
]);

onMounted(loadDashboard);

async function loadDashboard() {
  loading.value = true;
  try {
    const [sessionPage, logPage, modelPage, promptPage] = await Promise.all([
      fetchDiagnosisSessions({ pageNum: 1, pageSize: 3 }),
      fetchAiCallLogs({ pageNum: 1, pageSize: 8 }),
      fetchModelConfigs({ pageNum: 1, pageSize: 1 }),
      fetchPromptTemplates({ pageNum: 1, pageSize: 1 }),
    ]);
    recentSessions.value = sessionPage.records;
    recentLogs.value = logPage.records;
    aiLogTotal.value = logPage.total;
    modelConfigTotal.value = modelPage.total;
    promptTemplateTotal.value = promptPage.total;
  } catch (error) {
    message.error(error instanceof Error ? error.message : '加载工作台数据失败');
  } finally {
    loading.value = false;
  }
}

function riskColor(level: string) {
  const colors: Record<string, string> = {
    LOW: 'green',
    MEDIUM: 'blue',
    HIGH: 'orange',
    CRITICAL: 'red',
  };
  return colors[level] || 'default';
}

function statusLabel(status: string) {
  const labels: Record<string, string> = {
    ANALYZING: '分析中',
    WAITING_CONFIRM: '待确认',
    RESOLVED: '已解决',
    UNRESOLVED: '未解决',
    ARCHIVED: '已归档',
  };
  return labels[status] || status;
}
</script>
