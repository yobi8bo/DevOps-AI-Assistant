<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h1>案例库</h1>
        <p>沉淀典型故障处理过程，支持审核、发布和后续知识库转入。</p>
      </div>
      <a-space>
        <a-button @click="loadCases">
          <reload-outlined />
          刷新
        </a-button>
        <a-button type="primary" @click="openCreate">
          <plus-outlined />
          新增案例
        </a-button>
      </a-space>
    </div>

    <a-card :bordered="false" class="filter-card">
      <a-form layout="inline">
        <a-form-item label="关键词">
          <a-input v-model:value="query.keyword" allow-clear placeholder="标题、现象、原因或方案" @pressEnter="handleSearch" />
        </a-form-item>
        <a-form-item label="类型">
          <a-select v-model:value="query.category" allow-clear :options="categoryOptions" class="filter-select" />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="query.status" allow-clear :options="statusOptions" class="filter-select" />
        </a-form-item>
        <a-form-item label="标签">
          <a-input v-model:value="query.tag" allow-clear placeholder="标签关键词" @pressEnter="handleSearch" />
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
        :data-source="cases"
        :loading="loading"
        :pagination="pagination"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'title'">
            <a-button type="link" class="table-link" @click="openDetail(record.id)">
              {{ record.title }}
            </a-button>
            <div class="muted-line">{{ record.environment || '-' }}</div>
          </template>
          <template v-else-if="column.key === 'status'">
            <a-tag :color="statusColor(record.status)">{{ statusLabel(record.status) }}</a-tag>
          </template>
          <template v-else-if="column.key === 'tags'">
            <a-space wrap>
              <a-tag v-for="tag in record.tags" :key="tag">{{ tag }}</a-tag>
              <span v-if="!record.tags?.length" class="muted-line">-</span>
            </a-space>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button size="small" @click="openDetail(record.id)">查看</a-button>
              <a-button size="small" @click="openEdit(record.id)">编辑</a-button>
              <a-button
                v-if="record.status === 'PUBLISHED'"
                size="small"
                :loading="knowledgeSavingId === record.id"
                @click="transferToKnowledge(record.id)"
              >
                转知识库
              </a-button>
              <a-dropdown>
                <a-button size="small">状态</a-button>
                <template #overlay>
                  <a-menu @click="changeStatus(record.id, $event.key as CaseStatus)">
                    <a-menu-item v-for="item in statusOptions" :key="item.value">
                      {{ item.label }}
                    </a-menu-item>
                  </a-menu>
                </template>
              </a-dropdown>
              <a-popconfirm title="确认删除这个案例？" @confirm="removeCase(record.id)">
                <a-button size="small" danger>删除</a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-drawer
      v-model:open="detailOpen"
      width="min(960px, 100vw)"
      :title="detail?.title || '案例详情'"
      :destroy-on-close="true"
    >
      <a-spin :spinning="detailLoading">
        <a-empty v-if="!detail" description="暂无详情" />
        <div v-else class="detail-content">
          <a-descriptions bordered size="small" :column="2">
            <a-descriptions-item label="故障类型">{{ detail.category || '-' }}</a-descriptions-item>
            <a-descriptions-item label="状态">
              <a-tag :color="statusColor(detail.status)">{{ statusLabel(detail.status) }}</a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="运行环境">{{ detail.environment || '-' }}</a-descriptions-item>
            <a-descriptions-item label="来源会话">{{ detail.sourceSessionId || '-' }}</a-descriptions-item>
            <a-descriptions-item label="标签" :span="2">
              <a-space wrap>
                <a-tag v-for="tag in detail.tags" :key="tag">{{ tag }}</a-tag>
                <span v-if="!detail.tags?.length">-</span>
              </a-space>
            </a-descriptions-item>
          </a-descriptions>

          <a-card title="故障现象" :bordered="false">
            <pre class="message-content">{{ detail.symptom || '-' }}</pre>
          </a-card>
          <a-card title="原因分析" :bordered="false">
            <pre class="message-content">{{ detail.causeAnalysis || '-' }}</pre>
          </a-card>
          <a-card title="处理方案" :bordered="false">
            <pre class="message-content">{{ detail.solution || '-' }}</pre>
          </a-card>
          <a-card title="预防措施" :bordered="false">
            <pre class="message-content">{{ detail.prevention || '-' }}</pre>
          </a-card>
          <a-card title="原始日志" :bordered="false">
            <pre class="message-content">{{ detail.logContent || '-' }}</pre>
          </a-card>
          <a-card v-if="detail.status === 'PUBLISHED'" :bordered="false">
            <a-button type="primary" :loading="knowledgeSavingId === detail.id" @click="transferToKnowledge(detail.id)">
              转入知识库
            </a-button>
          </a-card>
        </div>
      </a-spin>
    </a-drawer>

    <a-modal
      v-model:open="formOpen"
      :title="editingId ? '编辑案例' : '新增案例'"
      :confirm-loading="saving"
      width="860px"
      @ok="saveCase"
    >
      <a-form layout="vertical">
        <a-form-item label="案例标题" required>
          <a-input v-model:value="form.title" placeholder="例如：Ubuntu 普通用户执行 docker ps 报 permission denied" />
        </a-form-item>
        <a-row :gutter="16">
          <a-col :span="8">
            <a-form-item label="故障类型">
              <a-select v-model:value="form.category" allow-clear :options="categoryOptions" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="状态">
              <a-select v-model:value="form.status" :options="statusOptions" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="标签">
              <a-select
                v-model:value="form.tags"
                mode="tags"
                :token-separators="[',', '，']"
                placeholder="输入标签后回车"
              />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="运行环境">
          <a-input v-model:value="form.environment" placeholder="例如：Ubuntu 24.04 + Docker" />
        </a-form-item>
        <a-form-item label="故障现象">
          <a-textarea v-model:value="form.symptom" :rows="4" />
        </a-form-item>
        <a-form-item label="原因分析">
          <a-textarea v-model:value="form.causeAnalysis" :rows="4" />
        </a-form-item>
        <a-form-item label="处理方案">
          <a-textarea v-model:value="form.solution" :rows="5" />
        </a-form-item>
        <a-form-item label="预防措施">
          <a-textarea v-model:value="form.prevention" :rows="3" />
        </a-form-item>
        <a-form-item label="原始日志">
          <a-textarea v-model:value="form.logContent" :rows="5" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { message, type TablePaginationConfig } from 'ant-design-vue';
import { PlusOutlined, ReloadOutlined, SearchOutlined } from '@ant-design/icons-vue';
import { defaultCategoryOptions, fetchFaultCategoryOptions } from '@/api/fault-category';
import { createKnowledgeFromCase } from '@/api/knowledge';
import {
  createCase,
  deleteCase,
  fetchCase,
  fetchCases,
  updateCase,
  updateCaseStatus,
  type CaseDetail,
  type CasePayload,
  type CaseStatus,
  type CaseSummary,
} from '@/api/case';

const loading = ref(false);
const saving = ref(false);
const detailLoading = ref(false);
const detailOpen = ref(false);
const formOpen = ref(false);
const editingId = ref<number | null>(null);
const knowledgeSavingId = ref<number | null>(null);
const cases = ref<CaseSummary[]>([]);
const detail = ref<CaseDetail | null>(null);
const total = ref(0);

const query = reactive({
  keyword: '',
  category: undefined as string | undefined,
  status: undefined as CaseStatus | undefined,
  tag: '',
  pageNum: 1,
  pageSize: 10,
});

const form = reactive<CasePayload>({
  title: '',
  category: undefined,
  environment: '',
  symptom: '',
  logContent: '',
  causeAnalysis: '',
  solution: '',
  prevention: '',
  tags: [],
  status: 'DRAFT',
});

const categoryOptions = ref(defaultCategoryOptions);

const statusOptions: Array<{ label: string; value: CaseStatus }> = [
  { label: '草稿', value: 'DRAFT' },
  { label: '待审核', value: 'PENDING_REVIEW' },
  { label: '已发布', value: 'PUBLISHED' },
  { label: '已驳回', value: 'REJECTED' },
  { label: '已下架', value: 'OFFLINE' },
];

const columns = [
  { title: '案例标题', key: 'title', dataIndex: 'title' },
  { title: '类型', key: 'category', dataIndex: 'category', width: 130 },
  { title: '标签', key: 'tags', dataIndex: 'tags', width: 220 },
  { title: '状态', key: 'status', dataIndex: 'status', width: 110 },
  { title: '更新时间', key: 'updatedAt', dataIndex: 'updatedAt', width: 190 },
  { title: '操作', key: 'action', width: 260 },
];

const pagination = computed<TablePaginationConfig>(() => ({
  current: query.pageNum,
  pageSize: query.pageSize,
  total: total.value,
  showSizeChanger: true,
  showTotal: (count) => `共 ${count} 条`,
}));

onMounted(async () => {
  await Promise.all([loadCases(), loadCategoryOptions()]);
});

async function loadCategoryOptions() {
  try {
    const options = await fetchFaultCategoryOptions();
    categoryOptions.value = options.map((item) => ({ label: item.categoryName, value: item.categoryName }));
  } catch (error) {
    message.error(error instanceof Error ? error.message : '加载故障分类失败');
  }
}

async function loadCases() {
  loading.value = true;
  try {
    const page = await fetchCases({
      ...query,
      tag: query.tag || undefined,
    });
    cases.value = page.records;
    total.value = page.total;
    query.pageNum = page.pageNum;
    query.pageSize = page.pageSize;
  } catch (error) {
    message.error(error instanceof Error ? error.message : '加载案例库失败');
  } finally {
    loading.value = false;
  }
}

function handleSearch() {
  query.pageNum = 1;
  loadCases();
}

function resetSearch() {
  query.keyword = '';
  query.category = undefined;
  query.status = undefined;
  query.tag = '';
  query.pageNum = 1;
  loadCases();
}

function handleTableChange(page: TablePaginationConfig) {
  query.pageNum = page.current || 1;
  query.pageSize = page.pageSize || 10;
  loadCases();
}

async function openDetail(id: number) {
  detailOpen.value = true;
  detailLoading.value = true;
  detail.value = null;
  try {
    detail.value = await fetchCase(id);
  } catch (error) {
    message.error(error instanceof Error ? error.message : '加载案例详情失败');
  } finally {
    detailLoading.value = false;
  }
}

function openCreate() {
  editingId.value = null;
  Object.assign(form, {
    title: '',
    category: undefined,
    environment: '',
    symptom: '',
    logContent: '',
    causeAnalysis: '',
    solution: '',
    prevention: '',
    tags: [],
    status: 'DRAFT',
  });
  formOpen.value = true;
}

async function openEdit(id: number) {
  try {
    const item = await fetchCase(id);
    editingId.value = id;
    Object.assign(form, {
      title: item.title,
      category: item.category,
      environment: item.environment || '',
      symptom: item.symptom || '',
      logContent: item.logContent || '',
      causeAnalysis: item.causeAnalysis || '',
      solution: item.solution || '',
      prevention: item.prevention || '',
      tags: item.tags || [],
      status: item.status,
    });
    formOpen.value = true;
  } catch (error) {
    message.error(error instanceof Error ? error.message : '加载案例失败');
  }
}

async function saveCase() {
  if (!form.title?.trim()) {
    message.warning('请填写案例标题');
    return;
  }
  saving.value = true;
  try {
    const payload = {
      ...form,
      title: form.title.trim(),
      tags: form.tags || [],
    };
    if (editingId.value) {
      await updateCase(editingId.value, payload);
      message.success('案例已保存');
    } else {
      await createCase(payload);
      message.success('案例已创建');
    }
    formOpen.value = false;
    await loadCases();
  } catch (error) {
    message.error(error instanceof Error ? error.message : '保存案例失败');
  } finally {
    saving.value = false;
  }
}

async function changeStatus(id: number, status: CaseStatus) {
  try {
    await updateCaseStatus(id, status);
    message.success('案例状态已更新');
    await loadCases();
    if (detail.value?.id === id) {
      detail.value = await fetchCase(id);
    }
  } catch (error) {
    message.error(error instanceof Error ? error.message : '状态更新失败');
  }
}

async function removeCase(id: number) {
  try {
    await deleteCase(id);
    message.success('案例已删除');
    if (detail.value?.id === id) {
      detailOpen.value = false;
      detail.value = null;
    }
    await loadCases();
  } catch (error) {
    message.error(error instanceof Error ? error.message : '删除案例失败');
  }
}

async function transferToKnowledge(id: number) {
  knowledgeSavingId.value = id;
  try {
    await createKnowledgeFromCase(id);
    message.success('已转入知识库');
  } catch (error) {
    message.error(error instanceof Error ? error.message : '转入知识库失败');
  } finally {
    knowledgeSavingId.value = null;
  }
}

function statusLabel(status: CaseStatus) {
  return statusOptions.find((item) => item.value === status)?.label || status;
}

function statusColor(status: CaseStatus) {
  const colors: Record<CaseStatus, string> = {
    DRAFT: 'default',
    PENDING_REVIEW: 'orange',
    PUBLISHED: 'green',
    REJECTED: 'red',
    OFFLINE: 'purple',
  };
  return colors[status] || 'default';
}
</script>
